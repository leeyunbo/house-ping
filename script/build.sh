#!/bin/bash

###############################################################################
# Houseping 애플리케이션 빌드 스크립트
###############################################################################

# 스크립트 디렉토리 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# 설정
APP_NAME="houseping"
JAR_NAME="${APP_NAME}-0.0.1-SNAPSHOT.jar"
JAR_PATH="${PROJECT_DIR}/build/libs/${JAR_NAME}"

# 기본값: 테스트 스킵
SKIP_TEST=true

###############################################################################
# 함수 정의
###############################################################################

# 사용법 출력
usage() {
    cat << EOF
사용법: $0 [옵션]

옵션:
  -t, --with-test    테스트 포함하여 빌드
  -c, --clean-only   clean만 수행
  -h, --help         도움말 표시

예시:
  ./build.sh              # 테스트 스킵하고 빌드
  ./build.sh -t           # 테스트 포함하여 빌드
  ./build.sh --clean-only # clean만 수행
EOF
}

# clean 수행
do_clean() {
    echo "🧹 이전 빌드 정리 중..."
    cd "$PROJECT_DIR" || exit 1

    if ./gradlew clean; then
        echo "✅ Clean 완료"
        return 0
    else
        echo "❌ Clean 실패"
        return 1
    fi
}

# 빌드 수행
do_build() {
    echo "🔨 애플리케이션 빌드 중..."
    cd "$PROJECT_DIR" || exit 1

    # 빌드 명령어 구성
    BUILD_CMD="./gradlew build"

    if [ "$SKIP_TEST" = true ]; then
        BUILD_CMD="$BUILD_CMD -x test"
        echo "   (테스트 스킵)"
    else
        echo "   (테스트 포함)"
    fi

    # 빌드 시작 시간 기록
    START_TIME=$(date +%s)

    # 빌드 실행
    if eval "$BUILD_CMD"; then
        # 빌드 종료 시간
        END_TIME=$(date +%s)
        ELAPSED=$((END_TIME - START_TIME))

        echo ""
        echo "✅ 빌드 성공 (소요 시간: ${ELAPSED}초)"

        # JAR 파일 정보 출력
        if [ -f "$JAR_PATH" ]; then
            JAR_SIZE=$(du -h "$JAR_PATH" | cut -f1)
            echo ""
            echo "📦 생성된 JAR 파일:"
            echo "   경로: $JAR_PATH"
            echo "   크기: $JAR_SIZE"
        fi

        return 0
    else
        echo ""
        echo "❌ 빌드 실패"
        return 1
    fi
}

# 빌드 결과 검증
verify_build() {
    echo ""
    echo "🔍 빌드 결과 검증 중..."

    if [ ! -f "$JAR_PATH" ]; then
        echo "❌ JAR 파일이 생성되지 않았습니다."
        return 1
    fi

    # JAR 파일 내용 확인
    if command -v jar &> /dev/null; then
        echo "   JAR 파일 내용 확인..."

        # application.yml 확인
        if jar -tf "$JAR_PATH" | grep -q "application.yml"; then
            echo "   ✅ application.yml 포함됨"
        else
            echo "   ⚠️  application.yml이 없습니다"
        fi

        # MANIFEST.MF 확인
        if jar -tf "$JAR_PATH" | grep -q "MANIFEST.MF"; then
            echo "   ✅ MANIFEST.MF 포함됨"
        fi
    fi

    echo "✅ 빌드 검증 완료"
    return 0
}

###############################################################################
# 파라미터 파싱
###############################################################################

CLEAN_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--with-test)
            SKIP_TEST=false
            shift
            ;;
        -c|--clean-only)
            CLEAN_ONLY=true
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
echo "  Houseping 빌드 스크립트"
echo "=========================================="
echo ""

# Clean
if ! do_clean; then
    exit 1
fi

# Clean만 수행하는 경우
if [ "$CLEAN_ONLY" = true ]; then
    echo ""
    echo "=========================================="
    exit 0
fi

echo ""

# Build
if ! do_build; then
    exit 1
fi

# Verify
if ! verify_build; then
    exit 1
fi

echo ""
echo "=========================================="
echo "다음 단계:"
echo "  애플리케이션 시작: cd script && ./start.sh"
echo "  배포 (빌드+재시작): cd script && ./deploy.sh"
echo "=========================================="
