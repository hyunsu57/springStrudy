package dev.tumbling.springailab.ai;

import com.anthropic.models.messages.Model;
import dev.tumbling.springailab.apikey.ApiKeyStore;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Anthropic 모델 객체를 직접 만들고, 키가 바뀌면 재시작 없이 다시 만드는 제공자(방식 ②).
 *
 * <p>왜 자동 설정을 쓰지 않는가
 * <ul>
 *   <li>Spring AI 2.0.2의 {@link AnthropicChatModel}은 생성될 때 옵션의 apiKey를 읽어 Anthropic SDK 클라이언트를 만들고
 *       {@code final} 필드에 보관한다. 요청별 옵션에 apiKey를 넣어도 이미 만들어진 클라이언트는 바뀌지 않는다(jar 바이트코드로 확인).</li>
 *   <li>자동 설정이 만든 ChatModel 빈은 앱 시작 시점의 키로 고정된다. 그래서 {@code spring.ai.model.chat=none}으로 끄고
 *       이 클래스가 키별로 새 모델을 만든다.</li>
 *   <li>ChatModel을 스프링 빈으로 등록하지 않는 이유: 빈으로 등록하면 그것을 주입받은 쪽(예: 자동 설정 ChatClient.Builder)이
 *       옛 인스턴스를 계속 들고 있게 된다. 대신 사용하는 쪽이 매번 {@link #get()}으로 현재 모델을 받아 간다.</li>
 * </ul>
 *
 * <p>동작: {@link #get()}은 저장소의 현재 키를 읽어, 마지막으로 만든 모델의 키와 같으면 그 모델을 재사용하고 다르면 새로 만든다.
 * 키가 없으면 {@link ApiKeyMissingException}을 던진다. {@link #refresh()}는 캐시를 비워 다음 호출에서 다시 만들게 한다.
 */
@Component
public class AnthropicChatModelProvider {

	private static final Logger log = LoggerFactory.getLogger(AnthropicChatModelProvider.class);

	/** 어떤 키로 만든 모델인지 함께 기억해 둔다. 키 문자열 비교(equals)로 재생성 여부를 정한다. */
	private record Built(String apiKey, AnthropicChatModel model) {
	}

	private final ApiKeyStore store;
	private final String modelId;
	private final int maxTokens;
	private final AtomicReference<Built> current = new AtomicReference<>();

	public AnthropicChatModelProvider(
			ApiKeyStore store,
			@Value("${app.ai.anthropic.model:claude-opus-5-5}") String modelId,
			@Value("${app.ai.anthropic.max-tokens:1024}") int maxTokens) {
		this.store = store;
		this.modelId = modelId;
		this.maxTokens = maxTokens;
	}

	/** 현재 키로 만든 모델을 돌려준다. 키가 바뀌었으면 새로 만든다. */
	public ChatModel get() {
		ApiKeyStore.Resolved resolved = store.resolve();
		if (!resolved.present()) {
			throw new ApiKeyMissingException();
		}
		Built built = current.get();
		if (built != null && built.apiKey().equals(resolved.key())) {
			return built.model();
		}
		// 여러 요청이 동시에 들어와 모델을 두 번 만드는 일을 막는다. 모델 생성은 가끔만 일어나므로 잠금 비용은 문제되지 않는다.
		synchronized (this) {
			built = current.get();
			if (built != null && built.apiKey().equals(resolved.key())) {
				return built.model();
			}
			AnthropicChatModel model = build(resolved.key());
			current.set(new Built(resolved.key(), model));
			log.info("Anthropic 모델을 새로 만들었다. model={}, 키 출처={}", modelId, resolved.source());
			return model;
		}
	}

	/** 키가 저장된 직후 호출한다. 캐시를 비워 다음 {@link #get()}에서 새 키로 다시 만들게 한다. */
	public void refresh() {
		current.set(null);
	}

	/** 지금 만들어진 모델이 있는지. 상태 확인과 테스트용이다. */
	public boolean isBuilt() {
		return current.get() != null;
	}

	public String modelId() {
		return modelId;
	}

	/**
	 * 실제 모델 생성. 옵션에는 model·maxTokens·apiKey 세 가지만 넣는다.
	 * temperature·topP·topK나 thinkingDisabled 같은 값은 최신 Claude 모델이 거부할 수 있어 일부러 설정하지 않는다.
	 */
	AnthropicChatModel build(String apiKey) {
		AnthropicChatOptions options = AnthropicChatOptions.builder()
				.apiKey(apiKey)
				.model(Model.of(modelId))
				.maxTokens(maxTokens)
				.build();
		return AnthropicChatModel.builder().options(options).build();
	}
}
