package dev.tumbling.springailab.apikey;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * API 키를 파일에 저장하고 읽어 오는 보관소.
 *
 * <p>설계 요점
 * <ul>
 *   <li>저장 위치는 {@code app.secrets.file}(기본 {@code ./config/secrets.properties})이다. 실행 중인 앱은
 *       {@code src/main/resources}의 소스 파일이 아니라 빌드 사본이나 JAR 내부를 읽으므로 그쪽에 쓰면 의미가 없다.
 *       프로젝트 루트의 {@code config/}는 Spring Boot가 외부 설정 폴더로 기본 인식하는 위치이면서 .gitignore로 제외하기 쉽다.
 *       다만 Boot가 자동으로 읽는 파일명은 {@code application*}뿐이라 {@code secrets.properties}는 Boot가 읽지 않는다.
 *       이 클래스가 직접 읽는다.</li>
 *   <li>Spring은 설정 파일을 실행 중에 다시 읽지 않는다. 그래서 이 클래스가 파일을 직접 읽고 쓰며, 현재 값을 메모리에 들고 있다.
 *       모델 객체를 다시 만드는 일은 {@code AnthropicChatModelProvider}가 이 값을 보고 처리한다.</li>
 *   <li>파일에 키가 없으면 환경 변수({@code app.secrets.env-name}, 기본 {@code ANTHROPIC_API_KEY})를 대신 본다.
 *       환경 변수로 키를 준 사용자에게 입력창이 계속 뜨지 않게 하기 위해서다.</li>
 *   <li>키 값은 절대 로그에 남기지 않는다. 로그에는 파일 경로와 출처(FILE/ENV/NONE)만 쓴다.</li>
 * </ul>
 */
@Component
public class ApiKeyStore {

	private static final Logger log = LoggerFactory.getLogger(ApiKeyStore.class);

	/** properties 파일 안에서 키를 담는 항목 이름. */
	static final String FILE_PROPERTY = "anthropic.api-key";

	/** 키가 어디서 왔는지. 화면은 이 값으로 "환경 변수로 설정됨" 같은 안내를 할 수 있다. */
	public enum Source { FILE, ENV, NONE }

	/** 현재 해석된 키와 출처. {@code key}는 출처가 NONE이면 null이다. */
	public record Resolved(String key, Source source) {
		public boolean present() {
			return key != null;
		}
	}

	private final Path file;
	private final String envName;
	/** 환경 변수를 읽는 함수. 테스트에서 실제 환경 변수 대신 가짜 값을 넣기 위해 함수로 분리했다. */
	private final Function<String, String> env;
	/** 파일에서 읽은(또는 방금 저장한) 키. 여러 요청 스레드가 동시에 읽어도 안전하도록 AtomicReference를 쓴다. */
	private final AtomicReference<String> fileKey = new AtomicReference<>();

	// 생성자가 둘이라 스프링이 어느 것을 쓸지 모르므로 @Autowired 로 이쪽을 지정한다.
	@Autowired
	public ApiKeyStore(
			@Value("${app.secrets.file:./config/secrets.properties}") String filePath,
			@Value("${app.secrets.env-name:ANTHROPIC_API_KEY}") String envName) {
		this(filePath, envName, System::getenv);
	}

	/** 테스트용 생성자. 환경 변수 조회 방법을 바꿔 끼울 수 있다. */
	ApiKeyStore(String filePath, String envName, Function<String, String> env) {
		// 상대경로는 JVM 작업 디렉터리(user.dir) 기준으로 절대경로로 바꿔 둔다.
		// IDE 실행 구성의 작업 디렉터리가 다르면 다른 파일을 보게 되므로, 시작 로그로 실제 경로를 알려 준다.
		this.file = Path.of(filePath).toAbsolutePath().normalize();
		this.envName = envName;
		this.env = env;
		loadFromFile();
		log.info("API 키 저장 파일: {} (현재 출처: {})", this.file, resolve().source());
	}

	/**
	 * 현재 사용할 키를 돌려준다. 파일 값이 환경 변수보다 우선한다.
	 *
	 * <p>사용자가 파일을 손으로 지웠을 때 메모리 값만 남아 "파일에 저장된 키 사용 중"으로 계속 보이는 일을 막기 위해,
	 * 메모리에 값이 있어도 파일이 사라졌으면 키 없음으로 되돌린다(반증 검토자 지적). 파일 존재 확인은 비용이 작다.
	 */
	public Resolved resolve() {
		String fromFile = fileKey.get();
		if (hasText(fromFile) && !Files.exists(file)) {
			fileKey.compareAndSet(fromFile, null);
			fromFile = null;
		}
		if (hasText(fromFile)) {
			return new Resolved(fromFile, Source.FILE);
		}
		String fromEnv = env.apply(envName);
		if (hasText(fromEnv)) {
			return new Resolved(fromEnv.trim(), Source.ENV);
		}
		return new Resolved(null, Source.NONE);
	}

	/** 저장 파일의 절대경로. 화면 안내와 로그에 쓴다. */
	public Path file() {
		return file;
	}

	/**
	 * 키를 파일에 저장하고 메모리 값도 갱신한다.
	 *
	 * <p>임시 파일에 먼저 쓰고 원자적으로 바꿔치기(ATOMIC_MOVE)해서, 쓰는 도중 앱이 죽어도 반쯤 쓰인 파일이 남지 않게 한다.
	 * 검증(빈 값·공백 등)은 호출하는 쪽({@code ApiKeySetupController})에서 끝낸 뒤 넘어온다고 가정한다.
	 */
	public synchronized void save(String apiKey) throws IOException {
		Path parent = file.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		Properties props = new Properties();
		props.setProperty(FILE_PROPERTY, apiKey);
		Path temp = Files.createTempFile(parent, "secrets", ".tmp");
		try {
			try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
				props.store(writer, "spring-ai-lab API key. 이 파일은 커밋하지 않는다.");
			}
			try {
				Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			}
			catch (AtomicMoveNotSupportedException atomicNotSupported) {
				// 일부 파일 시스템은 ATOMIC_MOVE를 지원하지 않는다. 그 경우에만 일반 교체로 대신한다(다른 IOException은 그대로 올린다).
				Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		finally {
			// 교체에 실패하면 키가 평문으로 든 임시 파일이 남으므로 반드시 지운다(성공했으면 이미 이동되어 없다).
			Files.deleteIfExists(temp);
		}
		fileKey.set(apiKey);
		log.info("API 키를 파일에 저장했다: {}", file);
	}

	private void loadFromFile() {
		if (!Files.exists(file)) {
			return;
		}
		Properties props = new Properties();
		try {
			String content = Files.readString(file, StandardCharsets.UTF_8);
			// 메모장 등으로 저장하면 파일 맨 앞에 BOM(U+FEFF, 인코딩 표시 문자)이 붙어 키 이름이 달라진다. 떼어 낸다.
			if (!content.isEmpty() && content.charAt(0) == '﻿') {
				content = content.substring(1);
			}
			try (Reader reader = new StringReader(content)) {
				props.load(reader);
			}
		}
		catch (IOException | IllegalArgumentException e) {
			// 읽을 수 없거나(IOException) 내용이 깨진 경우(Properties 가 잘못된 \\u 이스케이프에 IllegalArgumentException 을 던진다)
			// "키 없음"으로 취급해 입력창이 다시 뜨게 한다. 앱 시작은 막지 않는다. 로그에는 파일 경로와 예외 종류만 남긴다.
			log.warn("API 키 파일을 읽지 못했다. 키 없음으로 처리한다: {} ({})", file, e.getClass().getSimpleName());
			return;
		}
		String value = props.getProperty(FILE_PROPERTY);
		if (hasText(value)) {
			fileKey.set(value.trim());
		}
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
