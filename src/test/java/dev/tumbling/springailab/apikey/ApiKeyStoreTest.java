package dev.tumbling.springailab.apikey;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 파일·환경 변수에서 키를 읽고 저장하는 동작을 네트워크 없이 검사한다. */
class ApiKeyStoreTest {

	private static final String ENV = "SPRING_AI_LAB_TEST_KEY";

	@TempDir
	Path tempDir;

	@Test
	void 파일도_환경변수도_없으면_NONE() {
		ApiKeyStore store = new ApiKeyStore(tempDir.resolve("config/secrets.properties").toString(), ENV, name -> null);

		ApiKeyStore.Resolved resolved = store.resolve();

		assertThat(resolved.present()).isFalse();
		assertThat(resolved.source()).isEqualTo(ApiKeyStore.Source.NONE);
	}

	@Test
	void 환경변수만_있으면_ENV() {
		Map<String, String> fakeEnv = Map.of(ENV, "  env-key-1234567890abcdef  ");
		ApiKeyStore store = new ApiKeyStore(tempDir.resolve("secrets.properties").toString(), ENV, fakeEnv::get);

		ApiKeyStore.Resolved resolved = store.resolve();

		assertThat(resolved.source()).isEqualTo(ApiKeyStore.Source.ENV);
		assertThat(resolved.key()).isEqualTo("env-key-1234567890abcdef");
	}

	@Test
	void 저장하면_파일이_생기고_파일값이_환경변수보다_우선한다() throws IOException {
		Path file = tempDir.resolve("config/secrets.properties");
		Map<String, String> fakeEnv = Map.of(ENV, "env-key-1234567890abcdef");
		ApiKeyStore store = new ApiKeyStore(file.toString(), ENV, fakeEnv::get);

		store.save("file-key-1234567890abcdef");

		assertThat(file).exists();
		assertThat(Files.readString(file)).contains("anthropic.api-key=file-key-1234567890abcdef");
		assertThat(store.resolve().source()).isEqualTo(ApiKeyStore.Source.FILE);
		assertThat(store.resolve().key()).isEqualTo("file-key-1234567890abcdef");
		// 임시 파일이 남아 있지 않아야 한다.
		try (var files = Files.list(file.getParent())) {
			assertThat(files.map(p -> p.getFileName().toString())).containsExactly("secrets.properties");
		}
	}

	@Test
	void 새로_만든_저장소는_기존_파일을_읽는다() throws IOException {
		Path file = tempDir.resolve("secrets.properties");
		new ApiKeyStore(file.toString(), ENV, name -> null).save("saved-key-1234567890abcdef");

		ApiKeyStore reloaded = new ApiKeyStore(file.toString(), ENV, name -> null);

		assertThat(reloaded.resolve().source()).isEqualTo(ApiKeyStore.Source.FILE);
		assertThat(reloaded.resolve().key()).isEqualTo("saved-key-1234567890abcdef");
	}

	@Test
	void 값이_빈_파일은_키_없음이다() throws IOException {
		Path file = tempDir.resolve("secrets.properties");
		Files.writeString(file, "anthropic.api-key=   \n");

		ApiKeyStore store = new ApiKeyStore(file.toString(), ENV, name -> null);

		assertThat(store.resolve().source()).isEqualTo(ApiKeyStore.Source.NONE);
	}

	@Test
	void 내용이_깨진_파일은_키_없음으로_취급하고_생성자가_예외를_던지지_않는다() throws IOException {
		Path file = tempDir.resolve("secrets.properties");
		// Properties 는 잘못된 \\u 이스케이프에 IllegalArgumentException 을 던진다. 앱 시작이 막히면 안 된다.
		Files.writeString(file, "anthropic.api-key=\\u12zz\n");

		ApiKeyStore store = new ApiKeyStore(file.toString(), ENV, name -> null);

		assertThat(store.resolve().source()).isEqualTo(ApiKeyStore.Source.NONE);
	}

	@Test
	void BOM이_붙은_파일도_읽는다() throws IOException {
		Path file = tempDir.resolve("secrets.properties");
		Files.writeString(file, "﻿anthropic.api-key=bom-key-1234567890abcdef\n");

		ApiKeyStore store = new ApiKeyStore(file.toString(), ENV, name -> null);

		assertThat(store.resolve().key()).isEqualTo("bom-key-1234567890abcdef");
	}

	@Test
	void 파일을_지우면_키_없음으로_돌아간다() throws IOException {
		Path file = tempDir.resolve("secrets.properties");
		ApiKeyStore store = new ApiKeyStore(file.toString(), ENV, name -> null);
		store.save("deleted-key-1234567890abcdef");
		assertThat(store.resolve().source()).isEqualTo(ApiKeyStore.Source.FILE);

		Files.delete(file);

		assertThat(store.resolve().source()).isEqualTo(ApiKeyStore.Source.NONE);
	}

	@Test
	void 특수문자가_든_키도_저장_후_그대로_읽힌다() throws IOException {
		Path file = tempDir.resolve("secrets.properties");
		String key = "sk-ant-=:#\\odd!key-1234567890abcdef";
		ApiKeyStore store = new ApiKeyStore(file.toString(), ENV, name -> null);

		store.save(key);

		assertThat(new ApiKeyStore(file.toString(), ENV, name -> null).resolve().key()).isEqualTo(key);
	}
}
