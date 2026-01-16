# Lychnos（书灯）
> 译文： 油灯 / 夜读之灯 / 古代哲人夜读时唯一的光源

在翻开书之前，先点一盏灯

# 快速开始

## WEB UI
访问地址：http://localhost:3000

### 技术栈

1. 核心框架
- **React 19** - UI 框架
- **TypeScript 5.9** - 类型安全
- **Vite 7** - 构建工具
- **React Router 7** - 路由管理

2. UI & 样式
- **Tailwind CSS 3** - 样式框架
- **Lucide React** - 图标库

3. 网络请求
- **Axios 1.13** - HTTP 客户端
- **vite-plugin-mock 3** - Mock 数据服务
- **mockjs 1.1** - Mock 数据生成

4. 开发工具
- **ESLint 9** - 代码规范
- **PostCSS + Autoprefixer** - CSS 处理

### 安装依赖
```bash
cd webui
npm install
```

### 开发模式
1. 使用 Mock 数据（推荐用于前端独立开发）
> Mock已停止维护,存在数据不一致问题
```bash
npm run dev:mock
```
2.  使用真实后端接口
```bash
npm run dev
```

### 构建生产版本

```bash
## 默认配置在 webui/.env
npm run build

## jar
RUN VITE_API_BASE_URL=/ VITE_USE_MOCK=false VITE_BASE_PATH=/ npm run build
```

## 后端接口

### 技术栈
JDK 17 + Spring Boot 3.4

### 开发模式
```bash
## 源码运行
mvn spring-boot:run 

## idea 运行
直接运行 LychnosApplication.main()
```
### 构建生产版本
```bash
mvn clean package -DskipTests
```


# build
> 构建docker项目，运行[docker-build.bat](docker-build.bat)进行构建镜像

## docker
`docker run -d -p 1250:1250 -e MYSQL_URL=192.168.1.71:3306 --name lychnos tannnn/lychnos:0.0.1`

## docker compose
[docker-compose.yml](docker-compose.yml)




# 备注

## 错误码说明
> 接口结构返回的code,并非http state

| HTTP Code | 业务 Code | 错误名称 | 说明 | 前端处理 |
|-----------|----------|---------|------|---------|
| 401 | TOKEN_ERROR | Token 校验失败 | Token 无效或解析失败 | 清除 token，跳转登录页 |
| 401 | REDIS_EXPIRED_USER | 登录失效 | Redis 中未找到对应登录信息 | 清除 token，跳转登录页 |
| 401 | REDIS_NO_USER | 非法登录 | Token 已被刷新或被其他登录覆盖 | 清除 token，跳转登录页 |
| 402 | SYS_AUTHORIZED_PAST | 授权过期 | 系统授权已过期 | 提示用户联系管理员 |
| 403 | UNAUTHENTICATED | 系统未授权 | 当前用户无访问权限 | 提示用户无权限 |
| 403 | UNAUTHENTICATED_PLATFORM | 非法令牌访问 | Token 与访问端不匹配 | 清除 token，跳转登录页 |


## 注意事项

1. **Mock 数据仅在开发环境生效**：生产构建会自动禁用 Mock (ps: Mock已停止维护,存在数据不一致问题)
3. **测试两种模式**：开发时建议在 Mock 和真实接口两种模式下都测试功能
4. **前端环境变量修改后需重启**：修改 `.env` 文件后需要重启开发服务器
