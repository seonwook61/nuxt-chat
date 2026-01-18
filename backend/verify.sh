#!/bin/bash

echo "==================================================="
echo "Phase 0, T0.1 검증 스크립트"
echo "==================================================="
echo ""

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 카운터
PASS=0
FAIL=0

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1 존재"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $1 없음"
        ((FAIL++))
    fi
}

check_directory() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1 폴더 존재"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $1 폴더 없음"
        ((FAIL++))
    fi
}

echo "1. 프로젝트 구조 확인"
echo "---------------------------------------------------"
check_file "build.gradle.kts"
check_file "settings.gradle.kts"
check_file "gradlew"
check_file "gradlew.bat"
check_file "gradle/wrapper/gradle-wrapper.properties"
check_file "gradle/wrapper/gradle-wrapper.jar"
check_file ".gitignore"
check_file ".env.example"
check_file "Dockerfile"
check_file "README.md"
echo ""

echo "2. 소스 코드 파일 확인"
echo "---------------------------------------------------"
check_file "src/main/java/com/example/chat/ChatApplication.java"
check_file "src/main/java/com/example/chat/controller/HealthController.java"
check_file "src/main/java/com/example/chat/config/KafkaConfig.java"
check_file "src/main/java/com/example/chat/config/RedisConfig.java"
check_file "src/main/java/com/example/chat/config/WebSocketConfig.java"
check_file "src/main/java/com/example/chat/config/SecurityConfig.java"
echo ""

echo "3. 설정 파일 확인"
echo "---------------------------------------------------"
check_file "src/main/resources/application.yml"
check_file "src/main/resources/application-dev.yml"
echo ""

echo "4. 테스트 폴더 구조 확인"
echo "---------------------------------------------------"
check_directory "src/test/java/com/example/chat"
echo ""

echo "5. Java 버전 확인"
echo "---------------------------------------------------"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        echo -e "${GREEN}✓${NC} Java $JAVA_VERSION 설치됨"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} Java 17+ 필요 (현재: $JAVA_VERSION)"
        ((FAIL++))
    fi
else
    echo -e "${YELLOW}⚠${NC} Java 미설치 - README.md 참고하여 설치 필요"
    echo "  Windows: choco install openjdk17"
    echo "  macOS: brew install openjdk@17"
    echo "  Linux: sudo apt-get install openjdk-17-jdk"
    ((FAIL++))
fi
echo ""

echo "==================================================="
echo "검증 결과"
echo "==================================================="
echo -e "${GREEN}통과:${NC} $PASS"
echo -e "${RED}실패:${NC} $FAIL"
echo ""

if [ $FAIL -eq 0 ] || [ $FAIL -eq 1 ]; then
    if command -v java &> /dev/null; then
        echo -e "${GREEN}Phase 0, T0.1 완료!${NC}"
        echo ""
        echo "다음 단계:"
        echo "1. ./gradlew bootRun (백엔드 실행)"
        echo "2. http://localhost:8080/health 접속 확인"
        echo "3. 예상 응답: {\"status\":\"ok\"}"
    else
        echo -e "${YELLOW}Java 설치 후 다음 명령어로 검증:${NC}"
        echo "  ./gradlew bootRun"
        echo "  curl http://localhost:8080/health"
    fi
else
    echo -e "${RED}실패한 항목을 확인하세요.${NC}"
    exit 1
fi
