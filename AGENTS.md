# 프로젝트 에이전트 규칙 (Claude Code · Codex 공용)

이 프로젝트는 Spring AI 학습용 실습 프로젝트다. Claude Code와 Codex(GPT)가 번갈아 작업하므로 아래 규칙을 지킨다.

## 공동 작업 이력 기록 (필수)

1. 작업을 시작하면 먼저 `WORKLOG.md`의 마지막 항목을 읽고 현재 상태·다음 행동을 확인한다.
2. 파일을 바꾸거나 검증을 수행한 세션이 끝나면 `WORKLOG.md` 맨 아래에 새 항목을 추가한다. 형식은 그 파일의 "항목 형식"을 따른다.
3. 담당 란에는 실제 작업 주체(Claude, Codex, 사용자)를 적는다. 다른 에이전트의 항목은 수정하지 않고, 정정은 새 항목으로 남긴다.
4. 검증 란에는 실제 실행한 명령과 결과만 적는다. 실행하지 않은 검증을 했다고 쓰지 않는다.
5. 질문·설명만 하고 파일 변경이 없는 세션은 기록하지 않아도 된다.

## 환경

- 빌드 도구는 Gradle 9.8.2(Wrapper, `gradlew.bat`)다. Java 25(`C:\Users\hskim.ITEMBAY\.jdks\ms-25.0.4.1`)로 실행해야 하며 시스템 기본 java는 8이므로 세션에서 JAVA_HOME을 지정한다. 실행 방법은 `README.md`.
- 모델 API 키는 첫 실행 화면에서 입력받아 Spring 서버의 `config/secrets.properties`(gitignore 대상)에 저장하거나 환경 변수 `ANTHROPIC_API_KEY`로 공급한다. 소스·프론트 코드·`NEXT_PUBLIC_*`·커밋되는 설정 파일에는 넣지 않는다.
- 학습 문서는 itembay 저장소 `분석/공부/spring`, 진도 체크리스트는 `분석/공부/AI/[체크리스트] Java_AX_학습_진도.md`에 있다.
