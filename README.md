<div align="center">

# 🏠 Houseping

**청약 정보를 놓치지 마세요**

청약Home·LH 공공 API를 활용한 실시간 청약 알림 서비스

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

</div>

---

## 개요

대한민국의 청약 정보는 LH, 청약Home 등 여러 출처에 흩어져 있어 한눈에 파악하기 어렵습니다.

따라서 Houseping은 **청약Home, LH 공공 API**에서 데이터를 자동 수집하고, 다양한 방법을 통해 종합적인 청약 정보를 제공합니다.
- 매일 아침 9시 신규 청약 정보를 설정된 알림채널로 발송
- 관심 청약 구독 시 접수 시작 전날과 마감 당일 리마인드 알림
- 월별 청약 캘린더 제공
- 과거 청약 경쟁률 통계 및 트렌드 분석

Hexagonal Architecture를 적용하여 손쉽게 알림채널 및 청약 데이터 소스를 확장할 수 있습니다.

## 주요 기능

| 기능 | 설명 |
|------|------|
| **실시간 수집** | 청약Home, LH API 및 웹 캘린더에서 청약·경쟁률 데이터 자동 수집 |
| **일일 알림** | 매일 09:00 신규 청약 정보를 Slack/Telegram으로 발송 |
| **구독 알림** | 관심 청약 구독 시 접수 시작 전날, 마감 당일 리마인드 |
| **청약 캘린더** | 월별 캘린더에서 접수·당첨 일정 한눈에 확인 |
| **경쟁률 분석** | 과거 청약 경쟁률 통계 및 트렌드 분석 |
| **관리자 대시보드** | 데이터 검색, 필터링, 수동 수집 실행 |

## 스크린샷

| 통계 대시보드 | 청약 캘린더 |
|:---:|:---:|
| ![dashboard](image/dashboard.png) | ![calendar](image/calendar.png) |

| 청약 데이터 | 경쟁률 데이터 |
|:---:|:---:|
| ![subscriptions](image/subscriptions.png) | ![competition-rates](image/competition-rates.png) |

| 알림 구독 |
|:---:|
| ![subscribe](image/subscribe-modal.png) |

## 시작하기

### 1. 환경 변수 설정

```bash
cp .env.example .env
```

```properties
# 공공데이터포털 API 키 (https://data.go.kr)
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

### 3. 접속

- 관리자 대시보드: `http://localhost:10030/admin`
- 청약 캘린더: `http://localhost:10030/admin/subscriptions/calendar`
- 경쟁률 데이터: `http://localhost:10030/admin/competition-rates`
- 청약 데이터: `http://localhost:10030/admin/subscriptions`

## 설정

`application.yml`에서 데이터 소스 및 대상 지역을 설정합니다.

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

## License

MIT
