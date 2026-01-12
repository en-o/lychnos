# ============================================
# lychnos Docker 镜像构建文件
# `docker build -f apis/Dockerfile -t tannnn/lychnos:%VERSION% .`
# ============================================
# 构建方式：三阶段构建（前端构建 + 后端构建 + 运行）
# ============================================
# 阶段 1: 前端构建
# ============================================
FROM node:20-alpine AS frontend-builder

# 【可选】配置国内镜像源加速（中国区用户取消注释）
# Alpine Linux 使用阿里云镜像源
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
# 或使用腾讯云镜像源
# RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tencent.com/g' /etc/apk/repositories
# 或使用中科大镜像源
# RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories

# 配置 npm 使用淘宝镜像源
RUN npm config set registry https://registry.npmmirror.com

# 设置工作目录
WORKDIR /frontend

# 复制前端项目 package 文件
COPY webui/package*.json ./

# 安装依赖
RUN npm install

# 复制前端源代码
COPY webui/ ./

# 构建前端项目（生产模式，默认输出到 dist 目录）
# jar合并部署配置：
# - VITE_API_BASE_URL=/ : API 基础路径为根路径（前后端同域）
# - VITE_USE_MOCK=false : 不使用 Mock 数据
# - VITE_BASE_PATH=/ : 前端访问路由前缀为根路径
RUN VITE_API_BASE_URL=/ \
    VITE_USE_MOCK=false \
    VITE_BASE_PATH=/ \
    npm run build

# ============================================
# 阶段 2: 后端构建
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

# 【可选】配置国内镜像源加速（中国区用户取消注释）
# 阿里云镜像源
RUN sed -i 's@http://.*archive.ubuntu.com@http://mirrors.aliyun.com@g' /etc/apt/sources.list && \
    sed -i 's@http://.*security.ubuntu.com@http://mirrors.aliyun.com@g' /etc/apt/sources.list
# 或使用腾讯云镜像源
# RUN sed -i 's@http://.*archive.ubuntu.com@http://mirrors.tencent.com@g' /etc/apt/sources.list && \
#     sed -i 's@http://.*security.ubuntu.com@http://mirrors.tencent.com@g' /etc/apt/sources.list
# 或使用中科大镜像源
# RUN sed -i 's@http://.*archive.ubuntu.com@http://mirrors.ustc.edu.cn@g' /etc/apt/sources.list && \
#     sed -i 's@http://.*security.ubuntu.com@http://mirrors.ustc.edu.cn@g' /etc/apt/sources.list

# 设置工作目录
WORKDIR /app

# 复制后端项目 pom.xml
COPY apis/pom.xml ./

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B

# 复制后端源代码
COPY apis/src ./src

# 从前端构建阶段复制前端静态文件到后端资源目录
COPY --from=frontend-builder /frontend/dist ./src/main/resources/static

# 构建后端项目（打包成 jar）
RUN mvn clean package -DskipTests -B

# ============================================
# 阶段 3: 运行时镜像
# ============================================
FROM eclipse-temurin:17-jre-alpine

# 【可选】配置国内镜像源加速（中国区用户取消注释）
# Alpine Linux 使用阿里云镜像源
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
# 或使用腾讯云镜像源
# RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tencent.com/g' /etc/apk/repositories
# 或使用中科大镜像源
# RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories

# 添加维护者信息
LABEL maintainer="tannnn"
LABEL author="tannnn"
LABEL description="lychnos Service"
LABEL build.method="docker-build"

# 设置工作目录
WORKDIR /app

# 从后端构建阶段复制 jar 文件
COPY --from=backend-builder /app/target/*.jar /app/api.jar

# 创建数据目录
RUN mkdir -p /app/db

# 暴露应用端口
EXPOSE 1250

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:1250/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/api.jar"]
