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
1. **LH**: Web → API → DB
2. **ApplyHome**: API → DB

---

## 외부 API 연동 상세

### 1. 청약Home API (공공데이터포털)

**Base URL**: `https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1`

#### 엔드포인트
| 엔드포인트 | 설명 |
|-----------|------|
| `/getAPTLttotPblancDetail` | 아파트 청약 상세 |
| `/getRemndrLttotPblancDetail` | 잔여세대 청약 상세 |

#### 요청 파라미터
```
page: 페이지 번호
perPage: 페이지 크기
cond[HOUSE_SECD::EQ]: 주택 유형 코드 (01=APT, 10=민영사전청약, 12=신혼희망타운)
cond[SUBSCRPT_AREA_CODE_NM::EQ]: 지역명 (서울, 경기 등)
serviceKey: API 인증키
```

#### 응답 데이터 (주요 필드)
```json
{
  "data": [
    {
      "HOUSE_NM": "아파트명",
      "HOUSE_SECD": "주택유형코드",
      "SUBSCRPT_AREA_CODE_NM": "지역명",
      "RCRIT_PBLANC_DE": "공고일",
      "RCEPT_BGNDE": "접수시작일",
      "RCEPT_ENDDE": "접수종료일",
      "PRZWNER_PRESNATN_DE": "당첨자발표일",
      "HMPG_ADRES": "홈페이지URL",
      "PBLANC_URL": "상세URL"
    }
  ]
}
```

#### 필터링 로직
- 전체 데이터 조회 후 **접수시작일(RCEPT_BGNDE) == targetDate** 인 것만 필터링

---

### 2. LH 공식 API (공공데이터포털)

**Base URL**: `http://apis.data.go.kr/B552555/lhLeaseNoticeInfo1/lhLeaseNoticeInfo1`

#### 요청 파라미터
```
serviceKey: API 인증키
PG_SZ: 페이지 크기
PAGE: 페이지 번호
UPP_AIS_TP_CD: 주택유형 (05=분양, 06=임대, 39=신혼희망타운)
CNP_CD: 지역코드
PAN_SS: 청약상태 (접수중)
```

#### 응답 데이터 구조
```json
[
  { "header": "..." },
  {
    "dsList": [
      {
        "PAN_NM": "공고명",
        "CNP_CD_NM": "지역명",
        "UPP_AIS_TP_CD": "주택유형코드",
        "PAN_NT_ST_DT": "공고일",
        "CLSG_DT": "마감일",
        "PAN_SS": "접수상태",
        "DTL_URL": "상세URL"
      }
    ]
  }
]
```

#### 특이사항
- **접수시작일(receiptStartDate) 필드 없음** → 공고일로 대체
- 접수중 상태만 조회 가능

---

### 3. LH 웹 캘린더 API (비공식)

**Base URL**: `https://apply.lh.or.kr`
**Endpoint**: `/lhapply/apply/sc/detail.do`

#### 요청 방식
- **Method**: POST (Form Data)
- **Content-Type**: application/x-www-form-urlencoded

#### 요청 파라미터
```
panDt: 조회일자 (YYYYMMDD)
selectYear: 연도
selectMonth: 월
```

#### 응답 데이터
```json
{
  "panList": [
    {
      "panId": "공고ID",
      "panNm": "공고명",
      "panSs": "접수상태 (접수/접수마감)",
      "uppAisTpCd": "주택유형 (05=분양, 06=임대, 13=매입임대, 39=신혼희망타운)",
      "cnpCdNm": "지역명",
      "acpStDttm": "접수시작일 (YYYY.MM.DD)",
      "acpEdDttm": "접수종료일 (YYYY.MM.DD)",
      "panNtStDt": "공고일",
      "pzwrAncDt": "당첨자발표일"
    }
  ]
}
```

#### 필터링 로직
1. `panDt=targetDate`로 해당 날짜 관련 공고 조회
2. **접수시작일(acpStDttm) == targetDate** 인 것만 필터링
3. **지역(cnpCdNm)** 이 설정된 지역과 일치하는 것만 필터링

#### 특이사항
- **비공식 API** (웹 프론트엔드용)
- 공식 API에 없는 **접수시작일(acpStDttm)** 제공
- Fallback 우선순위: Web(1순위) → API(2순위) → DB(3순위)

---

## 데이터 수집 플로우

```
┌─────────────────────────────────────────────────────────────┐
│                    SubscriptionCollector                     │
│                     collect(targetDate)                      │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
┌─────────────────────────┐     ┌─────────────────────────┐
│   LH FallbackOrchestrator   │     │ ApplyHome FallbackOrchestrator │
└─────────────────────────┘     └─────────────────────────┘
              │                               │
    ┌─────────┼─────────┐           ┌────────┴────────┐
    ▼         ▼         ▼           ▼                 ▼
┌───────┐ ┌───────┐ ┌───────┐  ┌──────────┐    ┌──────────┐
│LH Web │→│LH API │→│LH DB  │  │ApplyHome │ →  │ApplyHome │
│@Order1│ │@Order2│ │@Order3│  │   API    │    │    DB    │
└───────┘ └───────┘ └───────┘  └──────────┘    └──────────┘
    │         │         │           │                 │
    └─────────┴─────────┴───────────┴─────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │ 중복 제거 (Set) │
                    └─────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │  알림 발송      │
                    │ (Telegram/Slack)│
                    └─────────────────┘
```