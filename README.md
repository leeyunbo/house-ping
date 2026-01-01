# 부동산 청약 알리미 서비스

## 개요
대한민국 청약Home API를 통해 실시간으로 신규 청약 정보를 수집하여 텔레그램으로 알림을 발송하는 서비스입니다.

## 아키텍처
- **헥사고날 아키텍처(Ports & Adapters)** 적용
- **클린 아키텍처** 원칙 준수
- **도메인 주도 설계(DDD)** 개념 적용

## 주요 기능
- 📊 청약Home API, LH API를 통한 실시간 청약 정보 수집
- 📱 텔레그램 봇을 통한 자동 알림 발송
- ⏰ 스케줄링 기반 자동 실행 (매일 09:00)
- 🔄 중복 알림 방지 및 실패 시 재시도
- 📈 알림 발송 이력 관리
- 🌐 REST API를 통한 수동 실행 지원

## 기술 스택
- **Framework**: Spring Boot 3.5.5
- **Language**: Java 21
- **Build Tool**: Gradle
- **Testing**: JUnit 5, Mockito, Spring Boot Test

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
- 저장된 청약 데이터를 키워드, 지역, 데이터 소스, 접수 기간으로 필터링하여 조회할 수 있습니다.

## 스케줄링
- **매일 09:00**: 신규 청약 정보 수집 및 알림 발송

## 테스트 실행
```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest

# 테스트 커버리지 확인
./gradlew jacocoTestReport
```

## 확장 계획
- 🎯 사용자별 맞춤 필터링 (지역 등)
- 📊 관리자 대시보드 구축
- 🔔 실시간 푸시 알림 지원

## 라이센스
MIT License

## 기여하기
1. Fork 프로젝트
2. Feature 브랜치 생성 (`git checkout -b feature/AmazingFeature`)
3. 변경사항 커밋 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 Push (`git push origin feature/AmazingFeature`)
5. Pull Request 생성

## 연락처
- 개발자: nodoyunbok@gmail.com
