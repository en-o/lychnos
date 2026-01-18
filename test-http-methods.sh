#!/bin/bash

# ========================================
# HTTP 方法过滤器测试脚本
# 用于测试 HttpMethodFilter 是否正常工作
# ========================================

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 默认测试地址
DEFAULT_LOCAL_URL="http://localhost:1250"
DEFAULT_ONLINE_URL="https://lychnos.tannn.cn"

# 使用方法
usage() {
    echo "用法: $0 [local|online|custom URL]"
    echo ""
    echo "示例:"
    echo "  $0                          # 测试本地环境 (http://localhost:1250)"
    echo "  $0 local                    # 测试本地环境 (http://localhost:1250)"
    echo "  $0 online                   # 测试线上环境 (https://lychnos.tannn.cn)"
    echo "  $0 http://test.com          # 测试自定义地址"
    echo ""
    exit 1
}

# 解析参数
if [ $# -eq 0 ]; then
    BASE_URL=$DEFAULT_LOCAL_URL
    ENV_NAME="本地"
elif [ "$1" = "local" ]; then
    BASE_URL=$DEFAULT_LOCAL_URL
    ENV_NAME="本地"
elif [ "$1" = "online" ]; then
    BASE_URL=$DEFAULT_ONLINE_URL
    ENV_NAME="线上"
elif [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    usage
else
    BASE_URL=$1
    ENV_NAME="自定义"
fi

# 打印测试信息
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}HTTP 方法过滤器测试${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "测试环境: ${YELLOW}${ENV_NAME}${NC}"
echo -e "测试地址: ${YELLOW}${BASE_URL}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 统计变量
PASS_COUNT=0
FAIL_COUNT=0

# 测试函数
test_method() {
    local method=$1
    local endpoint=$2
    local expected_status=$3
    local description=$4
    local data=$5

    local url="${BASE_URL}${endpoint}"

    echo -n "测试 ${method} ${endpoint} ... "

    # 构建 curl 命令
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X ${method} "${url}" \
            -H "Content-Type: application/json" \
            -d "${data}" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X ${method} "${url}" 2>&1)
    fi

    # 提取状态码（最后一行）
    status_code=$(echo "$response" | tail -n1)
    # 提取响应体（除了最后一行）
    body=$(echo "$response" | sed '$d')

    # 检查是否符合预期
    if [[ "$status_code" =~ ^$expected_status ]]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP ${status_code})"
        if [ "$expected_status" = "405" ]; then
            echo -e "  ${YELLOW}响应:${NC} ${body}"
        fi
        ((PASS_COUNT++))
    else
        echo -e "${RED}✗ FAIL${NC} (期望: ${expected_status}, 实际: ${status_code})"
        echo -e "  ${YELLOW}响应:${NC} ${body}"
        ((FAIL_COUNT++))
    fi
}

# ========================================
# 测试允许的方法（应该返回 2xx/3xx/4xx，但不是 405）
# ========================================
echo -e "${BLUE}=== 测试允许的 HTTP 方法 ===${NC}"
echo ""

test_method "GET" "/" "[2-4]" "获取首页"
test_method "POST" "/api/login" "[2-4]" "登录接口" '{"username":"test","password":"test123"}'
test_method "PUT" "/api/user/1" "[2-4]" "更新用户"
test_method "DELETE" "/api/user/1" "[2-4]" "删除用户"
test_method "PATCH" "/api/user/1" "[2-4]" "部分更新用户"
test_method "OPTIONS" "/" "[2-4]" "OPTIONS 请求"
test_method "HEAD" "/" "[2-4]" "HEAD 请求"

echo ""

# ========================================
# 测试被拒绝的方法（应该返回 405）
# ========================================
echo -e "${BLUE}=== 测试被拒绝的 HTTP 方法（WebDAV 扫描） ===${NC}"
echo ""

test_method "PROPFIND" "/" "405" "WebDAV PROPFIND (应被拒绝)"
test_method "PROPPATCH" "/" "405" "WebDAV PROPPATCH (应被拒绝)"
test_method "MKCOL" "/" "405" "WebDAV MKCOL (应被拒绝)"
test_method "COPY" "/" "405" "WebDAV COPY (应被拒绝)"
test_method "MOVE" "/" "405" "WebDAV MOVE (应被拒绝)"
test_method "LOCK" "/" "405" "WebDAV LOCK (应被拒绝)"
test_method "UNLOCK" "/" "405" "WebDAV UNLOCK (应被拒绝)"
test_method "TRACE" "/" "405" "TRACE (应被拒绝)"
test_method "CONNECT" "/" "405" "CONNECT (应被拒绝)"

echo ""

# ========================================
# 测试自定义非标准方法（应该返回 405）
# ========================================
echo -e "${BLUE}=== 测试自定义非标准方法 ===${NC}"
echo ""

test_method "CUSTOM" "/" "405" "自定义方法 CUSTOM (应被拒绝)"
test_method "INVALID" "/" "405" "自定义方法 INVALID (应被拒绝)"
test_method "HACKER" "/" "405" "自定义方法 HACKER (应被拒绝)"

echo ""

# ========================================
# 测试总结
# ========================================
TOTAL_COUNT=$((PASS_COUNT + FAIL_COUNT))

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}测试总结${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "总计: ${TOTAL_COUNT} 个测试"
echo -e "${GREEN}通过: ${PASS_COUNT}${NC}"
echo -e "${RED}失败: ${FAIL_COUNT}${NC}"
echo -e "${BLUE}========================================${NC}"

# 检查日志提示
echo ""
echo -e "${YELLOW}提示:${NC}"
echo -e "1. PROPFIND 等常见扫描方法被拒绝时，日志级别为 ${BLUE}DEBUG${NC}"
echo -e "   需要在 application.yaml 中设置日志级别才能看到："
echo -e "   ${BLUE}logging.level.cn.tannn.lychnos.common.filter.HttpMethodFilter: DEBUG${NC}"
echo ""
echo -e "2. CUSTOM/INVALID 等异常方法被拒绝时，日志级别为 ${YELLOW}WARN${NC}"
echo -e "   可以在日志中直接看到"
echo ""
echo -e "3. 查看日志命令："
echo -e "   本地: ${BLUE}tail -f logs/lychnos.log${NC}"
echo -e "   Docker: ${BLUE}docker logs -f lychnos${NC}"
echo ""

# 返回退出码
if [ $FAIL_COUNT -eq 0 ]; then
    exit 0
else
    exit 1
fi
