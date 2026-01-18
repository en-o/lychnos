@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

REM ========================================
REM HTTP 方法过滤器测试脚本 (Windows 版本)
REM 用于测试 HttpMethodFilter 是否正常工作
REM ========================================

set DEFAULT_LOCAL_URL=http://localhost:1250
set DEFAULT_ONLINE_URL=https://lychnos.tannn.cn

REM 解析参数
if "%1"=="" (
    set BASE_URL=%DEFAULT_LOCAL_URL%
    set ENV_NAME=本地
) else if "%1"=="local" (
    set BASE_URL=%DEFAULT_LOCAL_URL%
    set ENV_NAME=本地
) else if "%1"=="online" (
    set BASE_URL=%DEFAULT_ONLINE_URL%
    set ENV_NAME=线上
) else if "%1"=="-h" (
    goto :usage
) else if "%1"=="--help" (
    goto :usage
) else (
    set BASE_URL=%1
    set ENV_NAME=自定义
)

echo ========================================
echo HTTP 方法过滤器测试
echo ========================================
echo 测试环境: %ENV_NAME%
echo 测试地址: %BASE_URL%
echo ========================================
echo.

set PASS_COUNT=0
set FAIL_COUNT=0

REM ========================================
REM 测试允许的方法
REM 注意：这里只测试过滤器是否允许这些方法，不测试业务逻辑
REM 使用不存在的路径或根路径，避免影响真实数据
REM ========================================
echo === 测试允许的 HTTP 方法 ===
echo.

call :test_method GET / "GET 请求（应被允许）"
call :test_method POST /filter-test "POST 请求（应被允许）"
call :test_method PUT /filter-test "PUT 请求（应被允许）"
call :test_method DELETE /filter-test "DELETE 请求（应被允许）"
call :test_method PATCH /filter-test "PATCH 请求（应被允许）"
call :test_method OPTIONS / "OPTIONS 请求（应被允许）"
call :test_method HEAD / "HEAD 请求（应被允许）"

echo.

REM ========================================
REM 测试被拒绝的方法
REM ========================================
echo === 测试被拒绝的 HTTP 方法（WebDAV 扫描） ===
echo.

call :test_method PROPFIND / "WebDAV PROPFIND (应被拒绝)"
call :test_method PROPPATCH / "WebDAV PROPPATCH (应被拒绝)"
call :test_method MKCOL / "WebDAV MKCOL (应被拒绝)"
call :test_method COPY / "WebDAV COPY (应被拒绝)"
call :test_method MOVE / "WebDAV MOVE (应被拒绝)"
call :test_method LOCK / "WebDAV LOCK (应被拒绝)"
call :test_method UNLOCK / "WebDAV UNLOCK (应被拒绝)"
call :test_method TRACE / "TRACE (应被拒绝)"
call :test_method CONNECT / "CONNECT (应被拒绝)"

echo.

REM ========================================
REM 测试自定义非标准方法
REM ========================================
echo === 测试自定义非标准方法 ===
echo.

call :test_method CUSTOM / "自定义方法 CUSTOM (应被拒绝)"
call :test_method INVALID / "自定义方法 INVALID (应被拒绝)"
call :test_method HACKER / "自定义方法 HACKER (应被拒绝)"

echo.

REM ========================================
REM 测试总结
REM ========================================
set /a TOTAL_COUNT=%PASS_COUNT% + %FAIL_COUNT%

echo ========================================
echo 测试总结
echo ========================================
echo 总计: %TOTAL_COUNT% 个测试
echo 通过: %PASS_COUNT%
echo 失败: %FAIL_COUNT%
echo ========================================
echo.

echo 提示:
echo 1. PROPFIND 等常见扫描方法被拒绝时，日志级别为 DEBUG
echo    需要在 application.yaml 中设置日志级别才能看到
echo.
echo 2. CUSTOM/INVALID 等异常方法被拒绝时，日志级别为 WARN
echo    可以在日志中直接看到
echo.
echo 3. 查看日志命令:
echo    本地: tail -f logs/lychnos.log
echo    Docker: docker logs -f lychnos
echo.

if %FAIL_COUNT% GTR 0 (
    exit /b 1
) else (
    exit /b 0
)

:test_method
set METHOD=%1
set ENDPOINT=%2
set DESCRIPTION=%~3
set URL=%BASE_URL%%ENDPOINT%

echo 测试 %METHOD% %ENDPOINT% ...
curl -s -o nul -w "HTTP %%{http_code}" -X %METHOD% "%URL%" 2>nul
if %METHOD%==PROPFIND echo  - %DESCRIPTION%
if %METHOD%==PROPPATCH echo  - %DESCRIPTION%
if %METHOD%==MKCOL echo  - %DESCRIPTION%
if %METHOD%==COPY echo  - %DESCRIPTION%
if %METHOD%==MOVE echo  - %DESCRIPTION%
if %METHOD%==LOCK echo  - %DESCRIPTION%
if %METHOD%==UNLOCK echo  - %DESCRIPTION%
if %METHOD%==TRACE echo  - %DESCRIPTION%
if %METHOD%==CONNECT echo  - %DESCRIPTION%
if %METHOD%==CUSTOM echo  - %DESCRIPTION%
if %METHOD%==INVALID echo  - %DESCRIPTION%
if %METHOD%==HACKER echo  - %DESCRIPTION%
echo.
set /a PASS_COUNT+=1
goto :eof

:usage
echo 用法: %~nx0 [local^|online^|custom URL]
echo.
echo 示例:
echo   %~nx0                          # 测试本地环境 (http://localhost:1250)
echo   %~nx0 local                    # 测试本地环境 (http://localhost:1250)
echo   %~nx0 online                   # 测试线上环境 (https://lychnos.tannn.cn)
echo   %~nx0 http://test.com          # 测试自定义地址
echo.
exit /b 0
