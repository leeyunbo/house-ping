# 시작하기

## 요구사항

- Java 21+
- Gradle 8.x
- PostgreSQL 16+ (또는 Docker)
- 공공데이터포털 API 키 ([data.go.kr](https://data.go.kr))

## 1. 데이터베이스 설정

```bash
# Docker 사용 시
docker run -d --name houseping-db \
  -e POSTGRES_DB=houseping \
  -e POSTGRES_USER=your_user \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  postgres:16-alpine
```

## 2. 환경 변수 설정

```bash
cp .env.example .env
```

```properties
# 공공데이터포털 API 키
APPLYHOME_API_KEY=your_api_key
REAL_TRANSACTION_API_KEY=your_api_key

# 알림 설정 (선택)
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/xxx
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_CHAT_IDS="chat_id1,chat_id2"

# OAuth2 (선택)
NAVER_CLIENT_ID=your_client_id
NAVER_CLIENT_SECRET=your_client_secret
```

> 활용되는 모든 API에 대한 권한신청이 필요합니다.

## 3. 빌드 및 실행

```bash
./gradlew clean build
./gradlew :houseping-app:bootRun
```

http://localhost:8080 에서 확인할 수 있습니다.
