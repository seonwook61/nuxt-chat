#!/bin/bash

# Task 0.5.2 - Backend Test Verification Script
# This script verifies that all RED tests are properly created

echo "========================================="
echo "Task 0.5.2: Backend Test Verification"
echo "========================================="
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java installation
echo "1. Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo -e "${GREEN}✓${NC} Java installed: $JAVA_VERSION"
else
    echo -e "${RED}✗${NC} Java not found - Please install Java 17+"
    exit 1
fi

# Check Docker
echo ""
echo "2. Checking Docker..."
if command -v docker &> /dev/null; then
    if docker ps &> /dev/null; then
        echo -e "${GREEN}✓${NC} Docker is running"
    else
        echo -e "${YELLOW}⚠${NC} Docker is installed but not running"
        echo "   Please start Docker Desktop to run tests"
    fi
else
    echo -e "${RED}✗${NC} Docker not found - Required for Testcontainers"
    exit 1
fi

# Check test files
echo ""
echo "3. Verifying test files..."

TEST_FILES=(
    "src/test/java/com/example/chat/kafka/KafkaProducerServiceTest.java"
    "src/test/java/com/example/chat/kafka/KafkaConsumerServiceTest.java"
    "src/test/java/com/example/chat/redis/RedisCacheServiceTest.java"
    "src/test/java/com/example/chat/websocket/ChatWebSocketHandlerTest.java"
)

MISSING_FILES=0
for file in "${TEST_FILES[@]}"; do
    if [ -f "$file" ]; then
        LINE_COUNT=$(wc -l < "$file")
        echo -e "${GREEN}✓${NC} $file ($LINE_COUNT lines)"
    else
        echo -e "${RED}✗${NC} $file NOT FOUND"
        MISSING_FILES=$((MISSING_FILES + 1))
    fi
done

if [ $MISSING_FILES -gt 0 ]; then
    echo -e "${RED}ERROR: $MISSING_FILES test file(s) missing!${NC}"
    exit 1
fi

# Check model files
echo ""
echo "4. Verifying model files..."

MODEL_FILES=(
    "src/main/java/com/example/chat/model/ChatMessage.java"
    "src/main/java/com/example/chat/model/ChatEvent.java"
)

for file in "${MODEL_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $file"
    else
        echo -e "${RED}✗${NC} $file NOT FOUND"
        exit 1
    fi
done

# Check service interfaces
echo ""
echo "5. Verifying service interfaces..."

SERVICE_FILES=(
    "src/main/java/com/example/chat/service/KafkaProducerService.java"
    "src/main/java/com/example/chat/service/KafkaConsumerService.java"
    "src/main/java/com/example/chat/service/RedisCacheService.java"
)

for file in "${SERVICE_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $file"
    else
        echo -e "${RED}✗${NC} $file NOT FOUND"
        exit 1
    fi
done

# Check documentation
echo ""
echo "6. Verifying documentation..."

DOC_FILES=(
    "TEST_EXECUTION_GUIDE.md"
    "TEST_SUMMARY.md"
    "src/test/README.md"
)

for file in "${DOC_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $file"
    else
        echo -e "${RED}✗${NC} $file NOT FOUND"
    fi
done

# Summary
echo ""
echo "========================================="
echo "Summary"
echo "========================================="
echo -e "${GREEN}✓${NC} Total test files: ${#TEST_FILES[@]}"
echo -e "${GREEN}✓${NC} Total model files: ${#MODEL_FILES[@]}"
echo -e "${GREEN}✓${NC} Total service interfaces: ${#SERVICE_FILES[@]}"
echo ""

# Count test methods
echo "Test method count:"
KAFKA_PRODUCER_TESTS=$(grep -c "@Test" src/test/java/com/example/chat/kafka/KafkaProducerServiceTest.java 2>/dev/null || echo "0")
KAFKA_CONSUMER_TESTS=$(grep -c "@Test" src/test/java/com/example/chat/kafka/KafkaConsumerServiceTest.java 2>/dev/null || echo "0")
REDIS_TESTS=$(grep -c "@Test" src/test/java/com/example/chat/redis/RedisCacheServiceTest.java 2>/dev/null || echo "0")
WEBSOCKET_TESTS=$(grep -c "@Test" src/test/java/com/example/chat/websocket/ChatWebSocketHandlerTest.java 2>/dev/null || echo "0")

echo "  - KafkaProducerServiceTest: $KAFKA_PRODUCER_TESTS tests"
echo "  - KafkaConsumerServiceTest: $KAFKA_CONSUMER_TESTS tests"
echo "  - RedisCacheServiceTest: $REDIS_TESTS tests"
echo "  - ChatWebSocketHandlerTest: $WEBSOCKET_TESTS tests"

TOTAL_TESTS=$((KAFKA_PRODUCER_TESTS + KAFKA_CONSUMER_TESTS + REDIS_TESTS + WEBSOCKET_TESTS))
echo ""
echo -e "${GREEN}✓${NC} Total test methods: $TOTAL_TESTS"

# Test execution reminder
echo ""
echo "========================================="
echo "Next Steps"
echo "========================================="
echo ""
echo "To run tests (they will FAIL - this is expected!):"
echo -e "${YELLOW}  ./gradlew test${NC}"
echo ""
echo "To run individual test suites:"
echo "  ./gradlew test --tests KafkaProducerServiceTest"
echo "  ./gradlew test --tests KafkaConsumerServiceTest"
echo "  ./gradlew test --tests RedisCacheServiceTest"
echo "  ./gradlew test --tests ChatWebSocketHandlerTest"
echo ""
echo "View test report:"
echo "  build/reports/tests/test/index.html"
echo ""
echo -e "${GREEN}✓ Task 0.5.2 verification complete!${NC}"
echo "  All RED tests are ready for Phase 1 implementation."
echo ""
