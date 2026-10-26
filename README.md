# Spring AI Lab

Java 25 / Spring Boot 4.2.0-SNAPSHOT / Gradle 9.8.2 (Wrapper)

Spring Web MVC와 Validation 및 기본 컨텍스트 테스트를 포함합니다.
Spring AI 2.0.2 + Anthropic 스타터를 연결했고, API 키는 첫 실행 화면에서 입력받아 config/secrets.properties 에 저장합니다.

## 실행

IDE 에서 build.gradle 을 Gradle 프로젝트로 열고 프로젝트 SDK 와 Gradle JVM 을 Java 25 로 설정합니다(IntelliJ 재가져오기 절차는 아래).
현재 PC의 Java 25: C:\Users\hskim.ITEMBAY\.jdks\ms-25.0.4.1
PATH 의 java 는 Java 8, 전역 JAVA_HOME 은 JDK 21 입니다. 가장 확실한 방법은 프로젝트 폴더의 PowerShell 에서 아래처럼 Java 25 를 지정해 실행하는 것입니다.

```powershell
$env:JAVA_HOME = 'C:\Users\hskim.ITEMBAY\.jdks\ms-25.0.4.1'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat test
.\gradlew.bat bootRun
.\gradlew.bat bootJar   # 실행 JAR 생성 -> build\libs\spring-ai-lab-0.0.1-SNAPSHOT.jar
```

Gradle 9 자체는 JDK 17~27 이면 실행되고, 컴파일·테스트에는 Java 25 툴체인(build.gradle 이 요구하는 JDK)을 씁니다. 이 PC 는 전역 JAVA_HOME 이 JDK 21 이라 아무 설정 없이도 gradlew.bat 가 뜨고, Java 25 는 IntelliJ 가 등록해 둔 .jdks 폴더에서 자동 감지됩니다(반증 검토자 실측). 다만 JAVA_HOME 이 비어 있거나 Java 8 을 가리키면 "Gradle requires JVM 17 or later to run", Java 25 가 자동 감지되지 않는 PC 에서는 "Cannot find a Java installation ... languageVersion=25" 오류가 나므로, 위처럼 JAVA_HOME 을 25 로 지정하는 것이 가장 확실합니다. 처음 실행하면 Gradle 배포판과 의존성을 내려받습니다.
다른 폴더에서 gradlew.bat 를 호출하면 "does not contain a Gradle build" 로 시작 자체가 안 되므로 엉뚱한 config/ 를 볼 위험은 없습니다.

IntelliJ 는 아직 이 폴더를 Maven 프로젝트로 기억하고 있습니다(.idea/misc.xml 이 삭제된 pom.xml 을 가리킴). Maven 도구 창에서 프로젝트를 unlink 한 뒤 build.gradle 을 "Link Gradle Project" 하거나, 프로젝트를 닫고 .idea 를 지운 뒤 build.gradle 로 다시 여세요. 그 뒤 프로젝트 SDK 와 Gradle JVM 을 모두 Java 25 로 맞춥니다.
기본 포트는 8080, 종료는 Ctrl+C입니다. 루트 URL은 컨트롤러가 없어 404가 정상이며, API는 /api/health, /api/setup/status, /api/ai/summarize 입니다.
전역 JAVA_HOME과 PATH는 변경하지 않았습니다.

## 버전과 Spring AI

Initializr의 4.2.0.BUILD-SNAPSHOT 표기는 저장소에서 해석되지 않아 실제 좌표인 4.2.0-SNAPSHOT 을 씁니다. 2026-10-26 사용자 요청으로 Maven 에서 Gradle 9.8.2 로 전환했습니다(pom.xml → build.gradle, settings.gradle). Boot 스냅샷 플러그인 저장소는 settings.gradle 의 pluginManagement 에 있습니다.
스냅샷은 같은 버전 문자열이어도 내용이 바뀔 수 있습니다.
2026-10-26 사용자 선택(A)에 따라 Spring AI 2.0.2 BOM과 Anthropic 스타터를 추가했습니다.
Boot 4.2 조합은 공식 지원 범위 밖이며, 의존성 해석·컨텍스트 테스트·가짜 키로의 실제 호출(401)까지만 확인했습니다.

## API 키 첫 실행 입력 (방식 ②: 자동 설정 끄고 직접 재생성)

- 첫 화면(http://localhost:3000)에서 Spring의 GET /api/setup/status 가 configured=false 이면 키 입력창이 뜹니다.
- 저장하면 POST /api/setup/api-key 가 config/secrets.properties 에 키를 쓰고, AnthropicChatModelProvider 가 재시작 없이 새 키로 모델을 다시 만듭니다.
- config/ 는 .gitignore 로 제외됩니다. src/main/resources 에 쓰지 않는 이유와 설계 근거는 application.properties 와 각 클래스 주석에 있습니다.
- 파일에 키가 없으면 환경 변수 ANTHROPIC_API_KEY 를 대신 봅니다. 저장된 키를 바꾸려면 화면의 "키 변경" 버튼을 누릅니다.
- 모델은 app.ai.anthropic.model(기본 claude-opus-5-5)로 바꿉니다. 비용을 낮추려면 claude-sonnet-5-5 또는 claude-haiku-5-5.
- 요약 실습 API: POST /api/ai/summarize {"text": "..."} → {"summary": "...", "model": "..."}. 키가 없으면 409, 키가 거부되면 401이며 둘 다 needsApiKey=true 를 돌려줍니다.
- 실행 JAR는 프로젝트 루트에서 실행해야 같은 config/ 를 봅니다(java -jar build/libs/spring-ai-lab-0.0.1-SNAPSHOT.jar). gradlew.bat bootRun 은 항상 프로젝트 폴더 기준입니다.
- 저장 파일 위치를 바꾸려면 app.secrets.file 에 경로를 줍니다. properties 파일 안에서 Windows 절대경로를 쓸 때는 백슬래시가 이스케이프로 사라지므로 C:/Users/... 처럼 슬래시를 쓰거나 명령행 --app.secrets.file= 로 넘깁니다.
- config/secrets.properties 를 손으로 지우면 앱은 다음 요청부터 키 없음으로 돌아가 입력창이 다시 뜹니다. 저장된 키가 거부되면(401) 화면이 입력창을 강제로 다시 엽니다.

## Next.js 프론트엔드
frontend 폴더에서 npm.cmd run dev 실행 후 http://localhost:3000 접속.
Spring도 실행한 뒤 화면의 연결 확인 버튼으로 /api/health 요청을 보냅니다.
자세한 설정은 frontend/README.md를 참고합니다.


## 작업 이력

Claude Code와 Codex가 수행한 작업은 WORKLOG.md에 시간순으로 기록합니다. 규칙은 AGENTS.md를 참고합니다.

## 소스 읽는 순서 (학습용)

요청이 흘러가는 순서대로 읽는다. 각 파일 맨 위 주석에 "왜 이렇게 했는가"가 있다.

1. src/main/resources/application.properties — 자동 설정을 끄는 설정과 그 이유. 가장 짧고 전체 그림이 보인다.
2. build.gradle — Spring AI BOM(platform)과 Anthropic 스타터 줄, 그리고 settings.gradle 의 플러그인 저장소.
3. apikey/ApiKeyStore.java — 키 파일을 읽고 쓰는 곳. resolve()와 save()만 먼저 본다.
4. apikey/ApiKeySetupController.java — 화면이 부르는 두 API(status, api-key)와 입력 검사.
5. ai/AnthropicChatModelProvider.java — 이 실습의 핵심. get()이 키를 비교해 모델을 재사용하거나 새로 만든다.
6. ai/SummarizeController.java — 모델을 실제로 쓰는 곳. ChatClient 호출과 예외를 응답 코드로 바꾸는 부분.
7. frontend/src/app/page.tsx — 화면 상태 전이. fetchSetupStatus → 입력창 표시 → saveApiKey → summarize 순으로 따라간다.
8. 테스트를 같은 순서로 읽고 실행한다(gradlew.bat test). 일부러 하나를 고장 내 보면 각 테스트가 무엇을 지키는지 보인다.

한 요청을 끝까지 따라가 보기: 화면 "저장하기" → page.tsx saveApiKey → POST /api/setup/api-key → ApiKeySetupController.save → ApiKeyStore.save → AnthropicChatModelProvider.refresh. 그다음 "요약하기" → SummarizeController.summarize → provider.get() → build() → ChatClient.create(...).call().
