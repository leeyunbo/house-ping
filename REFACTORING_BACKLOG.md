# Houseping 리팩토링 백로그

## 아키텍처 위반

| # | 이슈 | 위치 | 심각도 |
|---|------|------|--------|
| A1 | BlogPostPersistencePort가 JPA Entity(BlogCardImageEntity) 직접 반환 | `core/port/BlogPostPersistencePort.java` | 높음 |
| A2 | PublicCalendarService가 QSubscriptionEntity 직접 import | `core/service/calendar/PublicCalendarService.java` | 높음 |
| A3 | AdminDataCollectionController가 ApplyhomeApiClient 직접 호출 | `admin/controller/web/AdminDataCollectionController.java` | 중간 |
| A4 | AdminNotificationController가 Slack/Telegram Formatter 직접 참조 | `admin/controller/web/AdminNotificationController.java` | 중간 |
| A5 | SubscriptionController가 SubscriptionMessageFormatter 직접 참조 | `admin/controller/api/SubscriptionController.java` | 중간 |
| A6 | HouseType enum에 API 경로(detailPath, pricePath) 포함 | `core/domain/HouseType.java:13-17` | 중간 |

## 도메인 모델

| # | 이슈 | 위치 | 심각도 |
|---|------|------|--------|
| D1 | CompetitionRate 필드 final 누락 (다른 도메인은 전부 immutable) | `core/domain/CompetitionRate.java` | 낮음 |
| D2 | User.updateLastLogin()이 LocalDateTime.now() 직접 호출 (테스트 불가) | `core/domain/User.java:48` | 낮음 |
| D3 | 도메인에 프레젠테이션 로직 (getTopAmountFormatted 등) | `SubscriptionPrice.java`, `RealTransaction.java` | 낮음 |
| D4 | BlogPost가 Anemic Domain Model (비즈니스 메서드 0개) | `core/domain/BlogPost.java` | 낮음 (비활성화) |

## 코드 품질

| # | 이슈 | 위치 | 심각도 |
|---|------|------|--------|
| Q1 | WebClient .block() → RestClient 전환 | `infrastructure/api/*.java` (전체) | 중간 |
| Q2 | DailyNotificationService JSON을 StringBuilder로 직접 조립 | `core/service/notification/DailyNotificationService.java` | 낮음 |
| Q3 | KakaoGeocodingClient 타임아웃 미설정 (defaultHttpClient 미적용) | `infrastructure/api/KakaoGeocodingClient.java` | 중간 |

## 운영 안정성

| # | 이슈 | 위치 | 심각도 |
|---|------|------|--------|
| O1 | API 수집 0건이어도 에러 알림 없음 (fetchSafely가 예외를 삼킴) | `ApplyhomeApiClient.java`, `LhWebScraperClient.java` | 높음 |
| O2 | 배포 시 다운타임 (docker stop → run) | `Jenkinsfile` | 낮음 |
