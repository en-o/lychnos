# 前端项目

基于 React + TypeScript + Vite 构建的图书阅读参考系统前端。

## 技术栈

- **React 18** - UI 框架
- **TypeScript** - 类型安全
- **Vite** - 构建工具
- **React Router** - 路由管理
- **Tailwind CSS** - 样式框架
- **Axios** - HTTP 客户端
- **Lucide React** - 图标库

## 功能特性

- ✅ JWT 登录鉴权
- ✅ 路由守卫
- ✅ 全局请求拦截和错误处理
- ✅ 图书智能分析
- ✅ 用户偏好学习
- ✅ 反馈历史记录
- ✅ 响应式设计

## 项目结构

```
webui/
├── src/
│   ├── api/           # API 接口层
│   │   ├── auth.ts    # 认证相关
│   │   └── book.ts    # 图书相关
│   ├── components/    # 公共组件
│   │   └── PrivateRoute.tsx  # 路由守卫
│   ├── pages/         # 页面组件
│   │   ├── LoginPage.tsx     # 登录页
│   │   └── HomePage.tsx      # 主页
│   ├── types/         # 类型定义
│   │   └── auth.ts    # 认证类型
│   ├── utils/         # 工具函数
│   │   └── request.ts # Axios 封装
│   ├── App.tsx        # 根组件
│   └── main.tsx       # 入口文件
├── .env               # 环境变量
└── vite.config.ts     # Vite 配置
```

## 快速开始

### 安装依赖

```bash
cd webui
npm install
```

### 开发模式

```bash
npm run dev
```

访问: http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 环境变量

创建 `.env` 文件配置后端 API 地址:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

## Mock 数据

当前版本使用 Mock 数据进行开发,后端接口可通过切换 API 文件实现:

- `mockAuthApi` → `authApi` (登录)
- `mockBookApi` → `bookApi` (图书)

## 登录信息

开发环境 Mock 登录:

- 用户名: `admin`
- 密码: `admin`

## JWT 认证

根据后端文档实现的认证机制:

### Token 处理

- Token 存储在 `localStorage`
- 请求拦截器自动添加 `token` 到请求头
- Token 失效自动跳转登录页

### 错误码处理

| HTTP Code | 业务 Code | 说明 |
|-----------|----------|------|
| 401 | TOKEN_ERROR | Token 无效 |
| 401 | REDIS_EXPIRED_USER | 登录失效 |
| 401 | REDIS_NO_USER | 非法登录 |
| 402 | SYS_AUTHORIZED_PAST | 授权过期 |
| 403 | UNAUTHENTICATED | 无访问权限 |
| 403 | UNAUTHENTICATED_PLATFORM | 非法令牌 |

### 全局拦截

所有 API 请求通过 `utils/request.ts` 统一处理:

- 自动添加 token
- 统一错误处理
- 自动跳转登录

## 后续集成

连接真实后端时需要:

1. 更新 `.env` 中的 `VITE_API_BASE_URL`
2. 将 API 文件中的 `mockAuthApi` 改为 `authApi`
3. 将 API 文件中的 `mockBookApi` 改为 `bookApi`
4. 根据实际后端接口调整请求参数和响应格式

## 注意事项

- 项目使用 Mock 数据,便于前端独立开发
- 真实接口需要后端配合调试
- 已配置代理,开发时可直接访问后端 API
- Tailwind CSS 已配置,可直接使用工具类

## License

MIT
