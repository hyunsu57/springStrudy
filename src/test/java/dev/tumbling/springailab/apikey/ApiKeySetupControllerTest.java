package dev.tumbling.springailab.apikey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 첫 실행 흐름을 HTTP 수준에서 검사한다: 상태 false → 잘못된 입력 400 → 올바른 입력 저장 → 상태 true.
 * 저장 파일은 임시 폴더로 돌리고, 환경 변수는 없는 이름을 보게 해서 이 PC의 실제 키 설정에 영향받지 않게 한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiKeySetupControllerTest {

	@TempDir
	static Path tempDir;

	@DynamicPropertySource
	static void secretsFile(DynamicPropertyRegistry registry) {
		registry.add("app.secrets.file", () -> tempDir.resolve("config/secrets.properties").toString());
		registry.add("app.secrets.env-name", () -> "SPRING_AI_LAB_NO_SUCH_ENV_VARIABLE");
	}

	@Autowired
	MockMvc mvc;

	@Test
	void 첫_실행_흐름() throws Exception {
		// 1) 아직 키가 없다 → 화면은 입력창을 보여 준다.
		mvc.perform(get("/api/setup/status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.configured").value(false))
				.andExpect(jsonPath("$.source").value("NONE"));

		// 2) 빈 값·짧은 값·공백 포함 값은 400이고 저장되지 않는다.
		mvc.perform(post("/api/setup/api-key").contentType(MediaType.APPLICATION_JSON).content("{\"apiKey\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").isString());
		mvc.perform(post("/api/setup/api-key").contentType(MediaType.APPLICATION_JSON).content("{\"apiKey\":\"short\"}"))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/api/setup/api-key").contentType(MediaType.APPLICATION_JSON)
						.content("{\"apiKey\":\"has space 1234567890abcdef\"}"))
				.andExpect(status().isBadRequest());
		// 줄바꿈 없는 공백(U+00A0)이나 비ASCII 문자가 섞인 키도 400 이어야 한다(로그 노출 경로 차단).
		mvc.perform(post("/api/setup/api-key").contentType(MediaType.APPLICATION_JSON)
						.content("{\"apiKey\":\"nbsp key-1234567890abcdef\"}"))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/api/setup/api-key").contentType(MediaType.APPLICATION_JSON)
						.content("{\"apiKey\":\"café-key-1234567890abcdef\"}"))
				.andExpect(status().isBadRequest());
		// apiKey 필드가 아예 없거나 null 이어도 500 이 아니라 400 이다.
		mvc.perform(post("/api/setup/api-key").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/api/setup/api-key").contentType(MediaType.APPLICATION_JSON).content("{\"apiKey\":null}"))
				.andExpect(status().isBadRequest());
		assertThat(tempDir.resolve("config/secrets.properties")).doesNotExist();

		// 3) 올바른 값은 저장되고, 응답과 이후 상태 조회가 모두 configured=true 다. 키 값은 응답 본문 어디에도 없다.
		mvc.perform(post("/api/setup/api-key").contentType(MediaType.APPLICATION_JSON)
						.content("{\"apiKey\":\"  test-key-1234567890abcdef  \"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.configured").value(true))
				.andExpect(jsonPath("$.source").value("FILE"))
				.andExpect(jsonPath("$.apiKey").doesNotExist())
				.andExpect(content().string(not(containsString("test-key-1234567890abcdef"))));
		mvc.perform(get("/api/setup/status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.configured").value(true));

		Path saved = tempDir.resolve("config/secrets.properties");
		assertThat(saved).exists();
		assertThat(Files.readString(saved)).contains("anthropic.api-key=test-key-1234567890abcdef");
	}
}
