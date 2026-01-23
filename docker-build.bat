chcp 65001 >nul
@echo off
REM ============================================
REM lychnos Docker 镜像构建脚本
REM 当前最新版本 0.0.2.6
REM 如果build有镜像拉取问题，使用下面的先拉一次试试
REM docker pull node:20-alpine && docker pull eclipse-temurin:17-jre-alpine && docker pull maven:3.9-eclipse-temurin-17
REM 确保 npm run build 和 mvn package 能够正确的执行（即项目能正确编译）
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

REM 清理历史编译产物，防止影响 Docker 构建
echo [清理中] 正在清理历史编译产物...
echo.

REM 清理前端编译产物
if exist "webui\dist" (
    echo - 清理前端 dist 目录
    rmdir /s /q "webui\dist"
)
if exist "webui\node_modules" (
    echo - 清理前端 node_modules 目录
    rmdir /s /q "webui\node_modules"
)

REM 清理后端编译产物
if exist "apis\target" (
    echo - 清理后端 target 目录
    rmdir /s /q "apis\target"
)

echo.
echo [清理完成] 历史编译产物已清理
echo.

REM 执行 Docker 构建
echo [构建中] 开始 Docker 镜像构建...
echo.
docker build -f apis/Dockerfile -t tannnn/lychnos:%VERSION% .

REM 检查构建结果
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo 构建成功！
    echo 镜像: tannnn/lychnos:%VERSION%
    echo ============================================
    echo.
    echo 运行命令: docker run -d -p 1250:1250 -e MYSQL_URL=127.0.0.1:3306 --name lychnos tannnn/lychnos:%VERSION%
) else (
    echo.
    echo ============================================
    echo 构建失败！错误代码: %ERRORLEVEL%
    echo ============================================
)

pause
