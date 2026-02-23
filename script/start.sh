#!/bin/bash

###############################################################################
# Houseping 애플리케이션 시작 스크립트
###############################################################################

# 스크립트 디렉토리 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# .env 파일 로드
if [ -f "$PROJECT_DIR/.env" ]; then
    echo "📁 환경변수 로드: $PROJECT_DIR/.env"
    set -a
    source "$PROJECT_DIR/.env"
    set +a
fi

# 설정
APP_NAME="houseping"
JAR_NAME="houseping-app-0.0.1-SNAPSHOT.jar"
JAR_PATH="${PROJECT_DIR}/houseping-app/build/libs/${JAR_NAME}"
PID_FILE="${SCRIPT_DIR}/${APP_NAME}.pid"
LOG_DIR="${PROJECT_DIR}/logs"
LOG_FILE="${LOG_DIR}/${APP_NAME}.log"

# JVM 옵션
JVM_OPTS="-Xms256m -Xmx512m"

# Spring Profile (기본값: local)
PROFILE="${SPRING_PROFILE:-local}"

###############################################################################
# 함수 정의
###############################################################################

# 로그 디렉토리 생성
ensure_log_dir() {
    if [ ! -d "$LOG_DIR" ]; then
        echo "📁 로그 디렉토리 생성: $LOG_DIR"
        mkdir -p "$LOG_DIR"
    fi
}

# 실행 중인 프로세스 확인
is_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            return 0
        else
            rm -f "$PID_FILE"
            return 1
        fi
    fi
    return 1
}

# JAR 파일 빌드
build_jar() {
    echo "🔨 애플리케이션 빌드 중..."
    cd "$PROJECT_DIR" || exit 1

    if ./gradlew clean build -x test; then
        echo "✅ 빌드 성공"
    else
        echo "❌ 빌드 실패"
        exit 1
    fi
}

# 애플리케이션 시작
start_app() {
    echo "🚀 $APP_NAME 시작 중..."
    echo "   프로파일: $PROFILE"
    echo "   JAR: $JAR_NAME"
    echo "   로그: $LOG_FILE"

    # 백그라운드로 실행
    nohup java $JVM_OPTS \
        -Dspring.profiles.active="$PROFILE" \
        -jar "$JAR_PATH" \
        >> "$LOG_FILE" 2>&1 &

    # PID 저장
    echo $! > "$PID_FILE"

    # 시작 대기
    sleep 3

    # 시작 확인
    if is_running; then
        PID=$(cat "$PID_FILE")
        echo "✅ $APP_NAME 시작 완료 (PID: $PID)"
        echo "📋 로그 확인: tail -f $LOG_FILE"
    else
        echo "❌ $APP_NAME 시작 실패"
        echo "📋 로그를 확인하세요: cat $LOG_FILE"
        exit 1
    fi
}

###############################################################################
# 메인 실행
###############################################################################

echo "=========================================="
echo "  Houseping 시작 스크립트"
echo "=========================================="

# 이미 실행 중인지 확인
if is_running; then
    PID=$(cat "$PID_FILE")
    echo "⚠️  $APP_NAME이 이미 실행 중입니다. (PID: $PID)"
    echo "   종료하려면 ./stop.sh 를 실행하세요."
    exit 1
fi

# 로그 디렉토리 생성
ensure_log_dir

# JAR 파일 확인 및 빌드
if [ ! -f "$JAR_PATH" ]; then
    echo "⚠️  JAR 파일이 없습니다. 빌드를 시작합니다..."
    build_jar
else
    echo "ℹ️  기존 JAR 파일을 사용합니다."
fi

# 애플리케이션 시작
start_app

echo "=========================================="
