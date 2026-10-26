package dev.tumbling.springailab.ai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

/** 키가 없을 때 요약 API가 409와 needsApiKey=true 를 돌려주는지, 입력 검사가 400을 주는지 검사한다. 모델 호출은 하지 않는다. */
@SpringBootTest
@AutoConfigureMockMvc
class SummarizeControllerTest {

	@TempDir
	static Path tempDir;

	@DynamicPropertySource
	static void secretsFile(DynamicPropertyRegistry registry) {
		registry.add("app.secrets.file", () -> tempDir.resolve("secrets.properties").toString());
		registry.add("app.secrets.env-name", () -> "SPRING_AI_LAB_NO_SUCH_ENV_VARIABLE");
	}

	@Autowired
	MockMvc mvc;

	@Test
	void 키가_없으면_409와_needsApiKey() throws Exception {
		mvc.perform(post("/api/ai/summarize").contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"요약할 글\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.needsApiKey").value(true))
				.andExpect(jsonPath("$.error").isString());
	}

	@Test
	void 빈_글은_400() throws Exception {
		mvc.perform(post("/api/ai/summarize").contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"   \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").isString());
	}
}
