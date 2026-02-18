<div align="center">

# 🏠 Houseping

**"이 청약, 넣을만할까?"**

분양가 vs 실거래가 비교 분석 서비스

[![Live Demo](https://img.shields.io/badge/Live-house--ping.com-ff6b6b)](https://house-ping.com)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Built with Claude](https://img.shields.io/badge/Built%20with-Claude-blueviolet?logo=anthropic)](https://claude.ai)

</div>

---

## 개요

청약 정보만으로는 "이 청약이 좋은 건지" 판단하기 어렵습니다.

Houseping은 **청약 분양가와 주변 실거래가를 비교 분석**하여, 예상 시세 차익을 한눈에 보여줍니다.

- 청약Home API에서 청약 정보 자동 수집
- 국토교통부 실거래가 API로 주변 시세 분석
- 분양가 vs 시세 비교로 예상 차익 계산

## 주요 기능

- **청약 목록** — 청약Home·LH 통합 수집, 마감/시작 임박순 정렬
- **가격 배지** — 신축 실거래 중앙값 기반 분양가 수준 3-state 판정 (시세대비↓/↑/비교불가)
- **시세 비교 분석** — 평형별 분양가 vs 동일 동 신축(5년) 실거래가 비교, 예상 차익 계산
- **실거래가 조회** — 법정동코드 파싱 → 국토부 API 캐시 → 동 단위 필터링
- **경쟁률 조회** — 발표 후 타입·순위·지역별 경쟁률
- **가점 계산기** — 청약 가점 항목별 계산
- **청약 가이드** — 청약 절차·용어 가이드 6페이지

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Backend** | Java 21, Spring Boot 3.5, Gradle |
| **Database** | PostgreSQL 16, JPA |
| **HTTP Client** | WebClient |
| **Notification** | Slack Webhook, Telegram Bot API |
| **Frontend** | Thymeleaf, FullCalendar.js |
| **Auth** | OAuth2 (Naver) |

## 패키지 구조

```
com.yunbok.houseping
├── core                            # 비즈니스 로직
│   ├── domain                      # 도메인 모델
│   ├── port                        # 포트 인터페이스 (NotificationSender, SubscriptionProvider 등)
│   └── service                     # 도메인 서비스 (subscription, notification, auth 등)
│
├── adapter                         # 외부 시스템 어댑터
│   ├── api                         # 외부 API (청약Home, LH, 국토부 실거래가)
│   ├── persistence                 # DB 조회 어댑터
│   └── notification                # 알림 (Slack, Telegram)
│
├── controller                      # 웹 계층
│   ├── web                         # 공개 페이지, 관리자 페이지
│   └── api                         # REST API
│
├── entity                          # JPA Entity
├── repository                      # Spring Data JPA Repository
├── scheduler                       # Quartz 스케줄러
├── config                          # 설정 (Security, WebClient, ProviderChain 등)
└── support                         # DTO, 유틸리티, 예외
```

## 시작하기

### 요구사항

- Java 21+
- Gradle 8.x
- PostgreSQL 16+ (또는 Docker)
- 공공데이터포털 API 키 ([data.go.kr](https://data.go.kr))

### 1. 데이터베이스 설정

```bash
# Docker 사용 시
docker run -d --name houseping-db \
  -e POSTGRES_DB=houseping \
  -e POSTGRES_USER=your_user \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. 환경 변수 설정

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
- 활용되는 모든 API에 대한 권한신청이 필요합니다.

## 아키텍처

초기 헥사고날 아키텍처에서 출발했으나, 도메인 영역이 좁아 완벽한 DIP 적용의 실익이 적다고 판단하여 실용적 레이어드로 전환했습니다.
변경 가능성이 높은 외부 연동(청약 API, 알림)에만 Port/Adapter를 유지하고, Repository 등은 직접 의존합니다.

```
  Controller  ─────▶   Service    ◀─────   Adapter (API, 알림)
  Scheduler              Port               Persistence
                        Domain              Repository (직접 의존)
```

### 확장 포인트

| 확장 | 구현 방법 |
|------|-----------|
| 알림 채널 추가 | `NotificationSender` 인터페이스 구현 후 설정 추가 |
| 데이터 소스 추가 | `SubscriptionProvider` 구현 후 `FallbackProviderChain`에 등록 |
