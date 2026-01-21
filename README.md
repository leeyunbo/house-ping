# 부동산 청약 알리미 서비스

## 개요
청약Home API, LH API를 통해 실시간으로 신규 청약 정보를 수집하여 Slack/Telegram으로 알림을 발송하는 서비스입니다.

## 주요 기능
- 청약Home API, LH API를 통한 실시간 청약 정보 수집
- LH 웹 캘린더를 통한 Fallback 데이터 수집
- Slack/Telegram 봇을 통한 자동 알림 발송
- 스케줄링 기반 자동 실행 (매일 09:00)
- DB 동기화 및 데이터 정리
- REST API를 통한 수동 실행 지원

## 기술 스택
- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Build Tool**: Gradle
- **Database**: H2
- **HTTP Client**: WebClient

## 프로젝트 구조
```
com.yunbok.houseping/
├── domain/                 # 핵심 비즈니스 로직
│   ├── model/              # 도메인 모델
│   ├── service/            # 도메인 서비스
│   └── port/
│       ├── in/             # 인바운드 포트 (UseCase)
│       └── out/            # 아웃바운드 포트 (Repository, Provider)
│
├── adapter/                # 포트 구현체
│   ├── in/
│   │   ├── web/            # REST Controller
│   │   └── scheduler/      # 스케줄러
│   └── out/
│       ├── api/            # 외부 API + DTO
│       ├── web/            # 웹 크롤링
│       ├── persistence/    # DB 어댑터
│       └── notification/   # 알림
│
└── infrastructure/         # 기술 설정
    ├── config/             # Spring 설정
    ├── persistence/        # Entity, Repository
    └── util/               # 유틸리티
```

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/subscriptions/collect` | 오늘 청약 수집 및 알림 발송 |
| GET | `/api/subscriptions/test?date=2025-01-01` | 특정 날짜 청약 조회 (테스트용) |
| POST | `/api/subscriptions/sync/initial` | DB 초기 동기화 |
| POST | `/api/subscriptions/cleanup` | 오래된 데이터 정리 |

## 실행 방법

### 개발 환경
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 운영 환경
```bash
./gradlew build
java -jar build/libs/houseping-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
```

### 관리자 페이지
- 서버 기동 후 `http://localhost:10030/admin/subscriptions` 접속
- 저장된 청약 데이터를 키워드, 지역, 데이터 소스, 접수 기간으로 필터링하여 조회

## 설정

### application.yml 주요 설정
```yaml
feature:
  subscription:
    lh-api-enabled: true
    lh-web-enabled: true
    lh-db-enabled: false
    applyhome-api-enabled: true
    applyhome-db-enabled: false

subscription:
  target-areas:
    - 서울
    - 경기
```

## 스케줄링
- **매일 09:00**: 신규 청약 정보 수집 및 알림 발송

## 데이터 소스 Fallback
1. **LH**: API → Web → DB
2. **ApplyHome**: API → DB

## 라이센스
MIT License
