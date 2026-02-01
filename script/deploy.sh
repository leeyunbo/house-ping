#!/bin/bash

###############################################################################
# Houseping 애플리케이션 배포 스크립트
# 빌드 → 재시작을 한번에 수행
###############################################################################

# 스크립트 디렉토리 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 설정
APP_NAME="houseping"
BACKUP_DIR="${SCRIPT_DIR}/backups"

# 기본값
SKIP_TEST=true
SKIP_BACKUP=false

###############################################################################
# 함수 정의
###############################################################################

# 사용법 출력
usage() {
    cat << EOF
사용법: $0 [옵션]

옵션:
  -t, --with-test      테스트 포함하여 빌드
  --no-backup          백업 생성 안 함
  -h, --help           도움말 표시

예시:
  ./deploy.sh              # 테스트 스킵, 백업 생성, 배포
  ./deploy.sh -t           # 테스트 포함하여 배포
  ./deploy.sh --no-backup  # 백업 없이 배포
EOF
}

# 현재 실행 중인 버전 백업
backup_current_jar() {
    if [ "$SKIP_BACKUP" = true ]; then
        echo "ℹ️  백업 스킵"
        return 0
    fi

    local JAR_PATH="../build/libs/${APP_NAME}-0.0.1-SNAPSHOT.jar"

    if [ ! -f "$JAR_PATH" ]; then
        echo "ℹ️  백업할 JAR 파일이 없습니다."
        return 0
    fi

    echo "💾 현재 JAR 파일 백업 중..."

    # 백업 디렉토리 생성
    mkdir -p "$BACKUP_DIR"

    # 타임스탬프로 백업 파일명 생성
    local TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    local BACKUP_FILE="${BACKUP_DIR}/${APP_NAME}_${TIMESTAMP}.jar"

    # 백업 수행
    if cp "$JAR_PATH" "$BACKUP_FILE"; then
        echo "✅ 백업 완료: $BACKUP_FILE"

        # 오래된 백업 정리 (최근 5개만 유지)
        echo "   오래된 백업 정리 중..."
        ls -t "${BACKUP_DIR}"/*.jar 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null
        local BACKUP_COUNT=$(ls -1 "${BACKUP_DIR}"/*.jar 2>/dev/null | wc -l | xargs)
        echo "   보관 중인 백업: ${BACKUP_COUNT}개"

        return 0
    else
        echo "⚠️  백업 실패 (계속 진행합니다)"
        return 0
    fi
}

# 기존 JAR 삭제
clean_jar() {
    local JAR_PATH="../build/libs/${APP_NAME}-0.0.1-SNAPSHOT.jar"
    if [ -f "$JAR_PATH" ]; then
        echo "🗑️  기존 JAR 파일 삭제..."
        rm -f "$JAR_PATH"
    fi
}

# 빌드 수행
do_build() {
    echo ""
    echo "1️⃣  빌드"
    echo "------------------------------------------"

    # 기존 JAR 삭제 후 새로 빌드
    clean_jar

    # build.sh 실행
    if [ "$SKIP_TEST" = true ]; then
        "$SCRIPT_DIR/build.sh"
    else
        "$SCRIPT_DIR/build.sh" -t
    fi

    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ 빌드 실패"
        return 1
    fi

    return 0
}

# 재시작 수행
do_restart() {
    echo ""
    echo "2️⃣  재시작"
    echo "------------------------------------------"

    "$SCRIPT_DIR/restart.sh"

    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ 재시작 실패"
        return 1
    fi

    return 0
}

# 배포 검증
verify_deployment() {
    echo ""
    echo "3️⃣  배포 검증"
    echo "------------------------------------------"

    # 3초 대기
    sleep 3

    # Health Check
    echo "Health Check 수행 중..."

    local APP_PORT="${APP_PORT:-10030}"
    local HEALTH_URL="http://localhost:${APP_PORT}/actuator/health"

    if command -v curl &> /dev/null; then
        local HEALTH_RESPONSE=$(curl -s "$HEALTH_URL" 2>/dev/null)

        if echo "$HEALTH_RESPONSE" | grep -q "UP"; then
            echo "✅ Health Check 성공"
            echo "$HEALTH_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$HEALTH_RESPONSE"
            return 0
        else
            echo "⚠️  Health Check 실패"
            echo "응답: $HEALTH_RESPONSE"
            return 1
        fi
    else
        echo "ℹ️  curl이 설치되어 있지 않아 Health Check를 건너뜁니다."
        return 0
    fi
}

###############################################################################
# 파라미터 파싱
###############################################################################

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--with-test)
            SKIP_TEST=false
            shift
            ;;
        --no-backup)
            SKIP_BACKUP=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "알 수 없는 옵션: $1"
            usage
            exit 1
            ;;
    esac
done

###############################################################################
# 메인 실행
###############################################################################

echo "=========================================="
echo "  Houseping 배포 스크립트"
echo "=========================================="
echo ""

# 배포 전 확인
echo "배포 옵션:"
echo "  테스트: $([ "$SKIP_TEST" = true ] && echo "스킵" || echo "포함")"
echo "  백업: $([ "$SKIP_BACKUP" = true ] && echo "안 함" || echo "생성")"
echo ""

# 백업
backup_current_jar

# 빌드
if ! do_build; then
    echo ""
    echo "=========================================="
    echo "❌ 배포 실패 (빌드 단계)"
    echo "=========================================="
    exit 1
fi

# 재시작
if ! do_restart; then
    echo ""
    echo "=========================================="
    echo "❌ 배포 실패 (재시작 단계)"
    echo ""
    echo "롤백 방법:"
    if [ "$SKIP_BACKUP" = false ]; then
        echo "  1. 최신 백업 확인: ls -lt $BACKUP_DIR"
        echo "  2. 백업 복원: cp $BACKUP_DIR/<백업파일> ../build/libs/"
        echo "  3. 재시작: ./restart.sh"
    else
        echo "  백업이 생성되지 않았습니다."
        echo "  Git으로 이전 버전 체크아웃 후 다시 빌드하세요."
    fi
    echo "=========================================="
    exit 1
fi

# 검증
verify_deployment

echo ""
echo "=========================================="
echo "✅ 배포 완료"
echo "=========================================="
echo ""
echo "다음 명령어:"
echo "  상태 확인: ./status.sh"
echo "  로그 확인: tail -f ../logs/houseping.log"
echo "=========================================="
