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

### 공개 페이지

| 기능 | 설명 |
|------|------|
| **청약 목록** | 서울/경기 임박한 청약 정보 확인 |
| **시세 비교 분석** | 평형별 분양가 vs 주변 실거래가 비교, 예상 차익 계산 |
| **실거래가 조회** | 동 단위 최근 3개월 실거래 데이터 |

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
├── domain                          # 핵심 비즈니스 로직
│   ├── model                       # 도메인 모델
│   ├── service                     # 도메인 서비스
│   └── port
│       ├── in                      # 인바운드 포트 (UseCase)
│       └── out                     # 아웃바운드 포트 (Repository, Provider)
│
├── adapter
│   ├── in
│   │   ├── web                     # REST Controller
│   │   │   ├── home                # 공개 페이지
│   │   │   └── admin               # 관리자 페이지
│   │   └── scheduler               # 스케줄러
│   └── out
│       ├── api                     # 외부 API (청약Home, 실거래가)
│       ├── web                     # 웹 파싱 (LH 캘린더)
│       ├── persistence             # DB 어댑터
│       └── notification            # 알림 (Slack, Telegram)
│
└── infrastructure                  # 설정, Entity, 유틸리티
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

### ~~Hexagonal Architecture~~ ![Deprecated](https://img.shields.io/badge/status-deprecated-red)
- ~~houseping 서비스는 외부 시스템과의 상호작용이 굉장히 다양하며 변경이 많은 서비스입니다.~~
- ~~따라서  헥사고날  아키텍처를 채택하여 외부 의존성과  변경이 적은 비즈니스 로직을 분리하여 테스트 용이성과 확장성을 확보했습니다.~~

```
[Adapter In]                [Domain]                 [Adapter Out]
  Controller  ─────▶  UseCase / Service  ◀─────      Repository
  Scheduler                   │                      API Client
                              │                      Notification
                         Domain Model
```

### Layered Architecture
- 초기에는 헥사고날 아키텍처를 채택했으나, 도메인 영역이 좁아 완벽한 DIP 적용의 실익이 적다고 판단
- 변경 가능성이 높은 외부 연동(청약 API, 알림 API)에만 Port/Adapter 적용
- 변경이 발생하지 않는 영역(Repository 등)은 직접 의존

```
  [External]              [Core]                    [External]
   Controller  ─────▶   Service    ◀─────             Adapter
   Scheduler             Port                      (API, DB, 알림)
                         Domain
```

### 책임 연쇄 패턴
- 데이터를 제공하는 외부 시스템이 불안정한 경우가 많습니다, 특히 최근 국가정보자원관리원 화재 사고시 모든 연동 시스템이 장기간 다운되는 사례가 있었습니다.
- 높은 가용성을 유지하기 위해 n개의 데이터 소스를 구비하여, 앞의 데이터 소스가 실패할 경우 다음 데이터 소스로 자동 전환하는 Fallback 메커니즘을 구현했습니다.
- 외부 시스템 1개마다 하나의 Chain이 구성되며, 손쉽게 새로운 Fallback 데이터 소스를 추가할 수 있습니다.

```
LH 웹 캘린더 ──▶ LH API ──▶ DB 캐시
     │            │          │
   성공 시        실패 시      실패 시
   반환          다음으로    최종 실패 처리 (장애 발생)
```
### 확장 포인트

| 확장 | 구현 방법                                                                     |
|------|---------------------------------------------------------------------------|
| 알림 채널 추가 | `NotificationSender` 인터페이스 구현 후, 설정 추가                                    |
| 데이터 소스 추가 | `SubscriptionProvider` 구현 후 해당되는 체인(`SubscriptionProviderChainConfig`)에 등록 |
