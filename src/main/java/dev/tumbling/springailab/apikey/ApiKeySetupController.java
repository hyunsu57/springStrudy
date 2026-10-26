package dev.tumbling.springailab.apikey;

import dev.tumbling.springailab.ai.AnthropicChatModelProvider;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 첫 실행 화면에서 API 키를 입력받는 API.
 *
 * <p>흐름: 화면이 뜰 때 {@code GET /api/setup/status}로 설정 여부만 묻는다 → {@code configured}가 false면 입력창을 보여 준다 →
 * 제출하면 {@code POST /api/setup/api-key}가 검사 후 파일에 저장하고 모델 제공자에게 "키가 바뀌었다"고 알린다 → 응답이 오면 입력창을 숨긴다.
 *
 * <p>어떤 응답에도 키 값은 넣지 않는다. 설정 여부와 출처, 저장 파일 경로만 돌려준다.
 */
@RestController
@RequestMapping("/api/setup")
public class ApiKeySetupController {

	/** 키 최소·최대 길이. 실제 키 형식은 제공자가 바꿀 수 있으므로 "빈 값·공백·터무니없는 길이"만 거른다. */
	static final int MIN_LENGTH = 20;
	static final int MAX_LENGTH = 512;

	private final ApiKeyStore store;
	private final AnthropicChatModelProvider provider;

	public ApiKeySetupController(ApiKeyStore store, AnthropicChatModelProvider provider) {
		this.store = store;
		this.provider = provider;
	}

	/** 화면이 입력창을 보일지 결정하는 데 쓰는 응답. */
	public record StatusResponse(boolean configured, ApiKeyStore.Source source, String file) {
	}

	public record SaveRequest(String apiKey) {
		/** record 의 기본 toString 은 키 값을 그대로 찍으므로(DEBUG 로그에 본문 객체가 출력될 수 있다) 가린다. */
		@Override
		public String toString() {
			return "SaveRequest[apiKey=" + (apiKey == null ? "null" : "****") + "]";
		}
	}

	@GetMapping("/status")
	public StatusResponse status() {
		ApiKeyStore.Resolved resolved = store.resolve();
		return new StatusResponse(resolved.present(), resolved.source(), store.file().toString());
	}

	@PostMapping("/api-key")
	public ResponseEntity<?> save(@RequestBody SaveRequest request) throws IOException {
		String error = validate(request == null ? null : request.apiKey());
		if (error != null) {
			// 400: 입력이 잘못됐다. 화면은 입력창을 그대로 두고 이 메시지를 보여 준다.
			return ResponseEntity.badRequest().body(Map.of("error", error));
		}
		String apiKey = request.apiKey().trim();
		store.save(apiKey);
		// 저장한 키로 모델을 다시 만들게 한다. 실제 생성은 다음 요약 요청 때 일어난다(지연 생성).
		provider.refresh();
		return ResponseEntity.ok(status());
	}

	/**
	 * 형식 검사. 문제가 없으면 null, 있으면 사용자에게 보여 줄 한국어 메시지를 돌려준다.
	 *
	 * <p>허용 문자를 눈에 보이는 ASCII(0x21~0x7E)로 제한하는 이유: 웹에서 복사할 때 딸려 오는 줄바꿈 없는 공백(U+00A0) 같은
	 * 문자는 공백 검사에 걸리지 않는데, 그런 키로 요청을 보내면 HTTP 클라이언트가 헤더 값 오류를 던지면서 예외 메시지에 키 전체를
	 * 넣는다. 저장 단계에서 걸러야 그 키가 로그로 흘러갈 길이 없다.
	 */
	static String validate(String apiKey) {
		if (apiKey == null || apiKey.isBlank()) {
			return "API 키를 입력해 주세요.";
		}
		String trimmed = apiKey.trim();
		for (int i = 0; i < trimmed.length(); i++) {
			char c = trimmed.charAt(i);
			if (c < 0x21 || c > 0x7E) {
				return "API 키에는 영문·숫자·기호만 들어갈 수 있습니다. 공백이나 특수 문자가 섞이지 않았는지 확인해 주세요.";
			}
		}
		if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
			return "API 키 길이가 올바르지 않습니다. 복사한 값을 다시 확인해 주세요.";
		}
		return null;
	}
}
