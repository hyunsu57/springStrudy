package dev.tumbling.springailab.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.tumbling.springailab.apikey.ApiKeyStore;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.model.ChatModel;

/** 키 유무·변경에 따라 모델이 만들어지고 다시 만들어지는지 검사한다. 모델 호출(네트워크)은 하지 않는다. */
class AnthropicChatModelProviderTest {

	@TempDir
	Path tempDir;

	/** 다른 패키지라 테스트용 생성자를 못 쓰므로 공개 생성자에 "없는 환경 변수 이름"을 줘서 이 PC의 실제 키를 보지 않게 한다. */
	private ApiKeyStore newStore() {
		return new ApiKeyStore(tempDir.resolve("secrets.properties").toString(), "SPRING_AI_LAB_NO_SUCH_ENV_VARIABLE");
	}

	@Test
	void 키가_없으면_예외를_던진다() {
		AnthropicChatModelProvider provider = new AnthropicChatModelProvider(newStore(), "claude-opus-5-5", 1024);

		assertThatThrownBy(provider::get).isInstanceOf(ApiKeyMissingException.class);
		assertThat(provider.isBuilt()).isFalse();
	}

	@Test
	void 같은_키면_같은_모델을_재사용하고_키가_바뀌면_새로_만든다() throws IOException {
		ApiKeyStore store = newStore();
		AnthropicChatModelProvider provider = new AnthropicChatModelProvider(store, "claude-opus-5-5", 1024);

		store.save("first-key-1234567890abcdef");
		ChatModel first = provider.get();
		ChatModel firstAgain = provider.get();

		// refresh() 없이 저장만 해도 키 문자열 비교로 새 모델이 만들어져야 한다(제공자 자체의 재생성 경로 검사).
		store.save("second-key-1234567890abcdef");
		ChatModel second = provider.get();

		// refresh() 를 부르면 같은 키라도 캐시를 비우므로 다시 만든다(컨트롤러가 저장 직후 부르는 경로).
		provider.refresh();
		ChatModel secondRebuilt = provider.get();

		assertThat(firstAgain).isSameAs(first);
		assertThat(second).isNotSameAs(first);
		assertThat(((AnthropicChatModel) second).getOptions().getApiKey()).isEqualTo("second-key-1234567890abcdef");
		assertThat(secondRebuilt).isNotSameAs(second);
		assertThat(provider.isBuilt()).isTrue();
	}

	@Test
	void 옵션에는_model_maxTokens_apiKey만_들어간다() throws IOException {
		ApiKeyStore store = newStore();
		store.save("option-key-1234567890abcdef");
		AnthropicChatModelProvider provider = new AnthropicChatModelProvider(store, "claude-opus-5-5", 777);

		AnthropicChatModel model = (AnthropicChatModel) provider.get();

		assertThat(model.getOptions().getModel()).isEqualTo("claude-opus-5-5");
		assertThat(model.getOptions().getMaxTokens()).isEqualTo(777);
		assertThat(model.getOptions().getApiKey()).isEqualTo("option-key-1234567890abcdef");
		// 최신 Claude 모델이 거부하는 샘플링 값은 설정하지 않았어야 한다.
		assertThat(model.getOptions().getTemperature()).isNull();
		assertThat(model.getOptions().getTopP()).isNull();
		assertThat(model.getOptions().getTopK()).isNull();
	}
}
