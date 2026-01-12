chcp 65001 >nul
@echo off
REM ============================================
REM lychnos Docker 镜像构建脚本
REM ============================================

REM 设置默认版本号
set VERSION=latest

REM 如果提供了参数，使用参数作为版本号
if not "%1"=="" set VERSION=%1

echo ============================================
echo 构建 lychnos Docker 镜像
echo 版本: %VERSION%
echo ============================================
echo.

REM 执行 Docker 构建
docker build -f apis/Dockerfile -t tannnn/lychnos:%VERSION% .

REM 检查构建结果
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo 构建成功！
    echo 镜像: tannnn/lychnos:%VERSION%
    echo ============================================
    echo.
    echo 运行命令: docker run -d -p 1250:1250 --name lychnos tannnn/lychnos:%VERSION%
) else (
    echo.
    echo ============================================
    echo 构建失败！错误代码: %ERRORLEVEL%
    echo ============================================
)

pause
