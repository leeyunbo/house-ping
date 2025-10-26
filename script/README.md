# Houseping 실행 스크립트

Houseping 애플리케이션을 관리하기 위한 스크립트 모음입니다.

## 스크립트 목록

### 1. build.sh - 애플리케이션 빌드
```bash
cd script
./build.sh              # 테스트 스킵하고 빌드
./build.sh -t           # 테스트 포함하여 빌드
./build.sh --clean-only # clean만 수행
```

**기능:**
- Gradle을 사용하여 프로젝트 빌드
- 테스트 포함/제외 옵션
- 빌드 결과 검증 (JAR 파일, application.yml 확인)
- 빌드 시간 측정

### 2. start.sh - 애플리케이션 시작
```bash
cd script
./start.sh
```

**기능:**
- JAR 파일이 없으면 자동으로 빌드
- 백그라운드로 실행
- PID 파일 생성 (`houseping.pid`)
- 로그 파일 생성 (`../logs/houseping.log`)
- 중복 실행 방지

**환경 변수:**
```bash
# Spring Profile 지정 (기본값: local)
SPRING_PROFILE=production ./start.sh
```

### 3. stop.sh - 애플리케이션 종료
```bash
cd script
./stop.sh
```

**기능:**
- 우아한 종료 (SIGTERM)
- 30초 타임아웃 후 강제 종료 (SIGKILL)
- PID 파일 자동 정리

### 4. restart.sh - 애플리케이션 재시작
```bash
cd script
./restart.sh
```

**기능:**
- stop.sh → start.sh 순차 실행
- 종료/시작 상태 확인

### 5. status.sh - 상태 확인
```bash
cd script
./status.sh
```

**출력 정보:**
- 실행 여부
- PID
- 실행 시간
- 메모리 사용량
- CPU 사용률
- Health Check (Actuator)
- 최근 로그 (마지막 10줄)

### 6. deploy.sh - 배포 (빌드 + 재시작)
```bash
cd script
./deploy.sh              # 테스트 스킵, 백업 생성, 배포
./deploy.sh -t           # 테스트 포함하여 배포
./deploy.sh --no-backup  # 백업 없이 배포
```

**기능:**
- 현재 JAR 파일 자동 백업 (최근 5개 유지)
- 빌드 수행
- 재시작
- 배포 검증 (Health Check)
- 배포 실패 시 롤백 방법 안내

**배포 프로세스:**
1. 백업: 현재 JAR 파일 백업
2. 빌드: 최신 코드 빌드
3. 재시작: 애플리케이션 종료 후 새 버전으로 시작
4. 검증: Health Check로 정상 동작 확인

## 사용 예시

### 기본 사용
```bash
# 애플리케이션 시작
cd script
./start.sh

# 상태 확인
./status.sh

# 로그 모니터링
tail -f ../logs/houseping.log

# 애플리케이션 종료
./stop.sh
```

### 개발 워크플로우
```bash
# 코드 수정 후 빌드
cd script
./build.sh

# 재시작
./restart.sh

# 또는 한번에 (빌드 + 재시작)
./deploy.sh
```

### 프로덕션 배포
```bash
# 전체 배포 프로세스 (권장)
cd script
./deploy.sh              # 백업 + 빌드 + 재시작 + 검증

# 또는 단계별 수행
./build.sh               # 1. 빌드
./status.sh              # 2. 현재 상태 확인
./restart.sh             # 3. 재시작

# 프로덕션 프로파일로 시작 (처음 시작 시)
SPRING_PROFILE=production ./start.sh
```

### 테스트 포함 빌드
```bash
cd script
./build.sh -t            # 테스트 포함 빌드
./deploy.sh -t           # 테스트 포함 배포
```

## 파일 구조

```
houseping/
├── script/
│   ├── build.sh          # 빌드 스크립트
│   ├── deploy.sh         # 배포 스크립트 (빌드 + 재시작)
│   ├── start.sh          # 시작 스크립트
│   ├── stop.sh           # 종료 스크립트
│   ├── restart.sh        # 재시작 스크립트
│   ├── status.sh         # 상태 확인 스크립트
│   ├── houseping.pid     # PID 파일 (자동 생성)
│   ├── backups/          # JAR 백업 디렉토리 (자동 생성)
│   │   └── houseping_YYYYMMDD_HHMMSS.jar
│   └── README.md         # 이 파일
├── logs/
│   └── houseping.log     # 로그 파일 (자동 생성)
└── build/libs/
    └── houseping-0.0.1-SNAPSHOT.jar
```

## 설정

### JVM 옵션 변경
`start.sh` 파일의 `JVM_OPTS` 변수를 수정하세요:

```bash
# start.sh
JVM_OPTS="-Xms256m -Xmx512m"
```

### 종료 타임아웃 변경
`stop.sh` 파일의 `STOP_TIMEOUT` 변수를 수정하세요:

```bash
# stop.sh
STOP_TIMEOUT=30  # 초 단위
```

## 트러블슈팅

### 배포 실패 시 롤백
```bash
# 1. 백업 파일 확인
ls -lt script/backups/

# 2. 최신 백업 복원
cp script/backups/houseping_YYYYMMDD_HHMMSS.jar build/libs/houseping-0.0.1-SNAPSHOT.jar

# 3. 재시작
cd script
./restart.sh
```

### 빌드 실패
```bash
# 빌드 캐시 삭제 후 재시도
./gradlew clean
cd script
./build.sh

# 또는
./build.sh --clean-only
./build.sh
```

### 포트 충돌
```bash
# 10030 포트를 사용 중인 프로세스 확인
lsof -i :10030

# 프로세스 종료
kill -9 <PID>

# 다른 포트로 실행하려면 application.yml 수정
# server.port: 10030
```

### PID 파일이 남아있는 경우
```bash
cd script
rm -f houseping.pid
./start.sh
```

### 로그 확인
```bash
# 전체 로그 보기
cat ../logs/houseping.log

# 실시간 로그 모니터링
tail -f ../logs/houseping.log

# 에러 로그만 확인
grep -i error ../logs/houseping.log

# 최근 100줄
tail -n 100 ../logs/houseping.log
```

### 수동으로 프로세스 종료
```bash
# PID 확인
cat houseping.pid

# 프로세스 종료
kill -15 <PID>

# 강제 종료
kill -9 <PID>

# PID 파일 삭제
rm -f houseping.pid
```

## 주의사항

1. **스크립트는 반드시 script 디렉토리에서 실행**하세요
2. **코드 수정 후에는 반드시 빌드 또는 배포 스크립트를 실행**하세요
   - application.yml 등 설정 파일 수정 시 특히 주의
   - `./deploy.sh` 또는 `./build.sh && ./restart.sh`
3. **production 환경에서는 적절한 Spring Profile을 지정**하세요
4. **로그 파일은 주기적으로 관리**하세요 (logrotate 등 사용)
5. **PID 파일은 수동으로 수정하지 마세요**
6. **배포 전 백업이 자동으로 생성**되지만, 중요한 변경은 Git 커밋을 권장합니다

## Health Check Endpoint

애플리케이션이 정상 실행 중이면 다음 엔드포인트로 상태를 확인할 수 있습니다:

```bash
# Health
curl http://localhost:10030/actuator/health

# Info
curl http://localhost:10030/actuator/info

# Metrics
curl http://localhost:10030/actuator/metrics
```

**포트 변경:**
- 기본 포트: `10030`
- 포트를 변경하려면 `src/main/resources/application.yml`에서 `server.port` 수정
- 또는 환경 변수로 설정: `APP_PORT=다른포트 ./status.sh`
