# Houseping

청약Home API, LH API를 통해 실시간으로 신규 청약 정보를 수집하여 Slack/Telegram으로 알림을 발송하는 서비스입니다.

## 주요 기능

- **청약 정보 수집**: 청약Home, LH 공공 API 및 웹 캘린더에서 데이터 수집
- **실시간 알림**: 매일 오전 9시 신규 청약 정보를 Slack/Telegram으로 자동 발송
- **경쟁률 분석**: 과거 청약 경쟁률 데이터 수집 및 통계 제공
- **관리자 대시보드**: 청약/경쟁률 데이터 조회 및 필터링

## 기술 스택

- Java 21, Spring Boot 3.x, Gradle
- H2 Database, QueryDSL, WebClient

## 시작하기

### 1. 환경 변수 설정

`.env.example`을 복사하여 `.env` 파일을 생성하고 API 키를 설정합니다.

```bash
cp .env.example .env
```

```properties
# 공공데이터포털 API 키
APPLYHOME_API_KEY=your_api_key
LH_API_KEY=your_api_key

# 알림 설정
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/xxx
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_CHAT_IDS="chat_id1,chat_id2"
```

### 2. 실행

```bash
# 개발 환경
./gradlew bootRun

# 운영 환경
./script/start.sh
```

### 3. 관리자 페이지

서버 기동 후 `http://localhost:10030/admin` 접속

## 설정

`application.yml`에서 데이터 소스 및 대상 지역을 설정할 수 있습니다.

```yaml
feature:
  subscription:
    lh-api-enabled: true
    lh-web-enabled: true
    applyhome-api-enabled: true

subscription:
  target-areas:
    - 서울
    - 경기
```