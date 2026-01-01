# Houseping 운영 가이드

## 프로세스 관리

### 시작
```bash
./script/start.sh
```

### 중지
```bash
./script/stop.sh
```

### 재시작
```bash
./script/restart.sh
```

### 상태 확인
```bash
./script/status.sh
```

## 빌드 & 배포

### 빌드
```bash
./script/build.sh
```

### 배포
```bash
./script/deploy.sh
```

## 로그

### 로그 위치
```
/Users/bok/Project/houseping/logs/houseping.log
```

### 실시간 로그 보기
```bash
tail -f /Users/bok/Project/houseping/logs/houseping.log
```

## 기타

### 프로파일
- 기본: local

### JAR 파일
- houseping-0.0.1-SNAPSHOT.jar
