#!/bin/bash

###############################################################################
# Houseping 애플리케이션 상태 확인 스크립트
###############################################################################

# 스크립트 디렉토리 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# 설정
APP_NAME="houseping"
PID_FILE="${SCRIPT_DIR}/${APP_NAME}.pid"
LOG_DIR="${PROJECT_DIR}/logs"
LOG_FILE="${LOG_DIR}/${APP_NAME}.log"
APP_PORT="${APP_PORT:-10030}"  # 환경 변수로 설정 가능, 기본값 10030

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
            return 1
        fi
    fi
    return 1
}

# 프로세스 정보 출력
show_process_info() {
    PID=$(cat "$PID_FILE")

    echo "📊 프로세스 정보"
    echo "------------------------------------------"
    echo "PID: $PID"

    # 실행 시간
    if command -v ps &> /dev/null; then
        ELAPSED=$(ps -p "$PID" -o etime= 2>/dev/null | xargs)
        echo "실행 시간: $ELAPSED"
    fi

    # 메모리 사용량 (macOS)
    if [[ "$OSTYPE" == "darwin"* ]]; then
        MEM=$(ps -p "$PID" -o rss= 2>/dev/null | xargs)
        if [ -n "$MEM" ]; then
            MEM_MB=$((MEM / 1024))
            echo "메모리 사용량: ${MEM_MB}MB"
        fi
    # 메모리 사용량 (Linux)
    else
        MEM=$(ps -p "$PID" -o rss= 2>/dev/null | xargs)
        if [ -n "$MEM" ]; then
            MEM_MB=$((MEM / 1024))
            echo "메모리 사용량: ${MEM_MB}MB"
        fi
    fi

    # CPU 사용률
    CPU=$(ps -p "$PID" -o %cpu= 2>/dev/null | xargs)
    if [ -n "$CPU" ]; then
        echo "CPU 사용률: ${CPU}%"
    fi

    echo ""
}

# 로그 파일 정보
show_log_info() {
    echo "📋 로그 정보"
    echo "------------------------------------------"

    if [ -f "$LOG_FILE" ]; then
        LOG_SIZE=$(du -h "$LOG_FILE" | cut -f1)
        LOG_LINES=$(wc -l < "$LOG_FILE" | xargs)
        echo "로그 파일: $LOG_FILE"
        echo "파일 크기: $LOG_SIZE"
        echo "라인 수: $LOG_LINES"
        echo ""
        echo "최근 로그 (마지막 10줄):"
        echo "------------------------------------------"
        tail -n 10 "$LOG_FILE"
    else
        echo "로그 파일이 없습니다."
    fi

    echo ""
}

# Health Check (Spring Actuator)
check_health() {
    echo "🏥 Health Check"
    echo "------------------------------------------"

    # actuator health 엔드포인트 확인
    if command -v curl &> /dev/null; then
        HEALTH=$(curl -s http://localhost:${APP_PORT}/actuator/health 2>/dev/null)

        if [ -n "$HEALTH" ]; then
            echo "$HEALTH" | python3 -m json.tool 2>/dev/null || echo "$HEALTH"
        else
            echo "Health 엔드포인트에 접근할 수 없습니다."
            echo "(애플리케이션이 완전히 시작되지 않았을 수 있습니다)"
        fi
    else
        echo "curl이 설치되어 있지 않아 Health Check를 수행할 수 없습니다."
    fi

    echo ""
}

###############################################################################
# 메인 실행
###############################################################################

echo "=========================================="
echo "  Houseping 상태 확인"
echo "=========================================="
echo "포트: $APP_PORT"
echo ""

# 실행 중인지 확인
if ! is_running; then
    echo "❌ $APP_NAME이 실행 중이지 않습니다."
    echo ""

    # PID 파일이 남아있다면 정리
    if [ -f "$PID_FILE" ]; then
        echo "⚠️  PID 파일이 남아있습니다. 정리합니다..."
        rm -f "$PID_FILE"
    fi

    echo "시작하려면: ./start.sh"
    echo "=========================================="
    exit 1
fi

# 실행 중
echo "✅ $APP_NAME이 실행 중입니다."
echo ""

# 프로세스 정보
show_process_info

# Health Check
check_health

# 로그 정보
show_log_info

echo "=========================================="
echo "명령어:"
echo "  로그 모니터링: tail -f $LOG_FILE"
echo "  애플리케이션 중지: ./stop.sh"
echo "  애플리케이션 재시작: ./restart.sh"
echo "=========================================="
