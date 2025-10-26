#!/bin/bash

###############################################################################
# Houseping 애플리케이션 종료 스크립트
###############################################################################

# 스크립트 디렉토리 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 설정
APP_NAME="houseping"
PID_FILE="${SCRIPT_DIR}/${APP_NAME}.pid"
STOP_TIMEOUT=30  # 강제 종료까지 대기 시간(초)

###############################################################################
# 함수 정의
###############################################################################

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

# 프로세스 종료 대기
wait_for_shutdown() {
    local pid=$1
    local timeout=$2
    local count=0

    while [ $count -lt $timeout ]; do
        if ! ps -p "$pid" > /dev/null 2>&1; then
            return 0
        fi
        sleep 1
        count=$((count + 1))
        if [ $((count % 5)) -eq 0 ]; then
            echo "   대기 중... ($count초)"
        fi
    done
    return 1
}

# 애플리케이션 종료
stop_app() {
    PID=$(cat "$PID_FILE")
    echo "🛑 $APP_NAME 종료 중... (PID: $PID)"

    # SIGTERM 시그널 전송 (우아한 종료)
    echo "   SIGTERM 시그널 전송..."
    kill -15 "$PID" 2>/dev/null

    # 종료 대기
    if wait_for_shutdown "$PID" "$STOP_TIMEOUT"; then
        echo "✅ $APP_NAME 정상 종료 완료"
        rm -f "$PID_FILE"
        return 0
    else
        # 타임아웃 시 강제 종료
        echo "⚠️  타임아웃: 강제 종료 시도 (SIGKILL)..."
        kill -9 "$PID" 2>/dev/null
        sleep 2

        if ! ps -p "$PID" > /dev/null 2>&1; then
            echo "✅ $APP_NAME 강제 종료 완료"
            rm -f "$PID_FILE"
            return 0
        else
            echo "❌ $APP_NAME 종료 실패"
            return 1
        fi
    fi
}

###############################################################################
# 메인 실행
###############################################################################

echo "=========================================="
echo "  Houseping 종료 스크립트"
echo "=========================================="

# 실행 중인지 확인
if ! is_running; then
    echo "ℹ️  $APP_NAME이 실행 중이지 않습니다."
    if [ -f "$PID_FILE" ]; then
        echo "   (PID 파일 정리 중...)"
        rm -f "$PID_FILE"
    fi
    exit 0
fi

# 애플리케이션 종료
stop_app

echo "=========================================="
