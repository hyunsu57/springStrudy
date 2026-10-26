# Spring AI Lab Frontend

Next.js App Router + TypeScript + Tailwind CSS + ESLint.

## 실행

이 frontend 폴더에서 npm.cmd run dev를 실행하고 http://localhost:3000 을 엽니다.
다른 터미널에서는 상위 프로젝트의 Java 25 설정 후 gradlew.bat bootRun 을 실행합니다.
홈 화면에서 연결 확인하기를 누르면 /api/health를 호출합니다.

## 요청 흐름

브라우저 → Next.js /api/* → rewrites → Spring http://127.0.0.1:8080/api/*
Spring의 /api/health는 서버 연결만 확인하며 AI 모델 상태를 검사하지 않습니다.
브라우저가 Spring 주소를 직접 호출하지 않아 이 경로에는 별도 브라우저 CORS 설정이 필요하지 않습니다.
rewrite는 인증·권한 검사를 대신하지 않습니다.

Spring 포트를 바꾸려면 .env.example을 .env.local로 복사해 BACKEND_URL을 변경하고 개발 서버를 재시작합니다. 프로덕션은 BACKEND_URL을 설정한 상태에서 다시 빌드한 뒤 시작합니다.
BACKEND_URL에는 후행 슬래시 없이 서버 origin을 입력합니다.
모델 API 키는 프론트에 넣지 않습니다. 첫 화면의 입력창으로 Spring 서버에 보내 config/secrets.properties 에 저장하거나, Spring 서버 환경 변수 ANTHROPIC_API_KEY 로 공급합니다.

## 확인 명령

npm.cmd run lint
npm.cmd exec tsc -- --noEmit
npm.cmd run build

의존성 버전은 package.json과 package-lock.json에 기록합니다.
AI 요약 기능은 아직 연결하지 않았습니다.
