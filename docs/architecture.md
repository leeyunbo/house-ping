# 아키텍처

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Backend** | Java 21, Spring Boot 3.5, Gradle |
| **Database** | PostgreSQL 16, JPA, QueryDSL |
| **HTTP Client** | WebClient |
| **Notification** | Slack Webhook, Telegram Bot API |
| **Frontend** | Thymeleaf, FullCalendar.js |
| **Auth** | OAuth2 (Naver) |

## 모듈 구조

```
houseping
├── houseping-core                  # 도메인, 서비스, DB, Port 인터페이스
│   ├── core/domain                 # 도메인 모델
│   ├── core/port                   # 외부 API Port 인터페이스
│   ├── core/service                # 도메인 서비스 (subscription, notification, auth 등)
│   ├── entity                      # JPA Entity
│   ├── repository                  # Spring Data JPA Repository
│   ├── persistence                 # DB 조회 (Store)
│   └── support                     # DTO, 유틸리티, 예외
│
├── houseping-external-api          # 외부 API 클라이언트 (Port 구현체)
│   └── externalapi
│       ├── (클라이언트)              # 청약Home, LH, 국토부 실거래가, 카카오, Claude
│       ├── dto                     # API 요청/응답 DTO
│       ├── formatter               # 알림 메시지 포맷터 (Slack, Telegram)
│       └── support                 # API 관련 유틸리티
│
├── houseping-web                   # 공개 웹 컨트롤러, Thymeleaf 템플릿
├── houseping-admin                 # 관리자 컨트롤러
└── houseping-app                   # Spring Boot 엔트리포인트, 스케줄러, 설정
```

## 모듈 의존 그래프

```
houseping-app   → core, external-api, web, admin
houseping-admin → core, external-api
houseping-web   → core
houseping-external-api → core
houseping-core  → (의존 없음)
```

DB는 서비스에 완전 종속된 영역이므로 core에 포함합니다.
외부 API만 변경 가능성이 높은 영역으로 분류하여 별도 모듈로 격리합니다.
Port 인터페이스는 외부 API 연동에만 적용합니다.

## 확장 포인트

| 확장 | 구현 방법 |
|------|-----------|
| 알림 채널 추가 | `NotificationSender` 인터페이스 구현 후 설정 추가 |
| 데이터 소스 추가 | `SubscriptionProvider` 구현 후 `FallbackProviderChain`에 등록 |

---

## 아키텍처 변천사

운영하면서 실제로 부딪힌 문제를 기반으로, 4단계에 걸쳐 구조를 개선했습니다.

### 1단계: 헥사고날 아키텍처 (1월)

프로젝트 초기에 헥사고날 아키텍처를 적용했습니다.

```
adapter/in/web/         ──▶  application/port/in/   ──▶  domain/
adapter/in/scheduler/        application/port/out/       application/service/
adapter/out/api/        ◀──  application/service/
adapter/out/persistence/
```

**문제점:** 청약 정보 조회라는 비교적 좁은 도메인에 in/out 포트, UseCase 인터페이스, 어댑터 디렉토리까지 풀세트로 갖추니 파일 수만 많아지고 실질적인 유연성은 얻지 못했습니다. 서비스 하나 수정하는데 3~4개 파일을 건드려야 했습니다.

**결정:** 헥사고날의 핵심 가치(외부 의존 역전)만 남기고, in/out 구분과 UseCase 인터페이스를 제거했습니다.

### 2단계: 실용적 레이어드 (2월 초)

```
core/domain/       # 도메인 모델
core/port/         # 변경 가능성 높은 외부 연동만 Port 유지
core/service/      # 서비스 (Port에 의존)
adapter/api/       # 외부 API 클라이언트
adapter/persistence/  # DB 어댑터
controller/        # 웹 계층
```

변경 가능성이 높은 외부 연동(청약 API, 알림)에만 Port/Adapter를 유지하고, Repository는 서비스에서 직접 의존하도록 단순화했습니다. `adapter` 네이밍을 `infrastructure`로 변경하고 `*Adapter` → `*Client`/`*Store`로 통일했습니다.

**남은 문제:** 레이어드로 단순화한 덕에 개발 속도는 빨라졌지만, 프로젝트가 커지면서 새로운 문제가 생겼습니다.
- 클래스가 많아지며 변경 영향도 파악이 어려웠고 의존성 관리가 어려워졌습니다.
- 단일 모듈이라 admin 코드가 production에 영향을 주며, 관리자 기능 수정이 공개 페이지 빌드에 영향을 줬습니다.

### 3단계: Gradle 멀티모듈 분리 (2월 중순)

admin과 production의 의존성을 컴파일 타임에 차단하기 위해 모듈을 분리했습니다.

```
houseping-core    # 도메인, 서비스, 인프라 (154 파일)
houseping-admin   # Admin 컨트롤러/템플릿 (18 파일)
houseping-app     # Spring Boot 진입점, Config, Scheduler (21 파일)
houseping-web     # 공개 웹 컨트롤러/템플릿
```

**남은 문제:** core에 비즈니스 로직과 infrastructure(API 클라이언트, Store)가 함께 있어서, 외부 API 변경 → core 변경 → web/admin 전부 리빌드되는 구조였습니다.

### 4단계: Infrastructure 모듈 분리 (2월 말)

core 서비스가 infrastructure 구현체에 직접 의존하던 것을 Port 인터페이스 기반으로 전환하고, infrastructure를 별도 모듈로 추출했습니다.

```
houseping-app   → core, infra, web, admin
houseping-admin → core, infra
houseping-web   → core
houseping-infra → core
houseping-core  → (의존 없음)
```

9개 Port 인터페이스를 도입하고 14개 서비스의 주입 타입을 concrete → interface로 변경했습니다. 외부 API 변경 시 infra만 수정하면 되어 web에 변경이 전파되지 않습니다.

**남은 문제:** DB 접근까지 Port/Adapter로 감싸면서 불필요한 간접 레이어가 생겼습니다. DB는 우리 서비스에 완전히 종속된 영역이라 변경 가능성이 0%에 가까운데, Entity ↔ Domain 변환 코드와 Persistence Port 인터페이스가 필드 추가할 때마다 3곳을 고치게 만들었습니다.

### 5단계: infra → external-api 재편 (현재)

"변경 가능성"을 기준으로 모듈을 재분류했습니다.

- **변경 가능성 낮음 (core):** 도메인, 서비스, DB(Entity/Repository/Store)
- **변경 가능성 높음 (external-api):** 외부 API 클라이언트

```
houseping-app          → core, external-api, web, admin
houseping-admin        → core, external-api
houseping-web          → core
houseping-external-api → core
houseping-core         → (의존 없음)
```

DB 관련 Persistence Port를 제거하고 서비스가 Repository/Store를 직접 사용하도록 단순화했습니다. Port 인터페이스는 외부 API 연동에만 남겨두어 실질적으로 변경 격리가 필요한 곳에만 추상화를 적용합니다.

### 요약

| 단계 | 구조 | 계기 |
|------|------|------|
| 1단계 | 헥사고날 풀세트 | 초기 설계 |
| 2단계 | 실용적 레이어드 | 도메인 대비 과한 추상화 제거 |
| 3단계 | Gradle 멀티모듈 (4개) | admin↔production 빌드 격리 |
| 4단계 | 5개 모듈 + Port 전면 적용 | infrastructure 변경 전파 차단 |
| 5단계 | external-api 분리 + DB Port 제거 | 변경 가능성 기준으로 재분류, 불필요한 간접 레이어 제거 |
