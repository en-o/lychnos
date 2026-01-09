# Lychnos 图书阅读参考系统 - 前端项目文档

> 基于 React + TypeScript + Vite 构建的智能图书阅读推荐系统

---

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [Mock 系统](#mock-系统)
- [API 接口文档](#api-接口文档)
- [错误码说明](#错误码说明)
- [开发指南](#开发指南)

---

## 项目概述

Lychnos 是一个智能图书阅读推荐系统,通过 AI 分析图书内容并根据用户偏好提供个性化推荐。

### 功能特性

- ✅ JWT 登录鉴权
- ✅ 路由守卫
- ✅ 全局请求拦截和错误处理
- ✅ 图书智能分析（AI驱动）
- ✅ 用户偏好学习
- ✅ 反馈历史记录
- ✅ 年度阅读报告
- ✅ 响应式设计
- ✅ 完整的 Mock 系统
- ✅ 便捷的真实/Mock 接口切换

---

## 技术栈

### 核心框架
- **React 19** - UI 框架
- **TypeScript 5.9** - 类型安全
- **Vite 7** - 构建工具
- **React Router 7** - 路由管理

### UI & 样式
- **Tailwind CSS 3** - 样式框架
- **Lucide React** - 图标库

### 网络请求
- **Axios 1.13** - HTTP 客户端
- **vite-plugin-mock 3** - Mock 数据服务
- **mockjs 1.1** - Mock 数据生成

### 开发工具
- **ESLint 9** - 代码规范
- **PostCSS + Autoprefixer** - CSS 处理

---

## 快速开始

### 安装依赖

```bash
cd webui
npm install
```

### 开发模式

#### 使用 Mock 数据（推荐用于前端独立开发）
```bash
npm run dev:mock
```

#### 使用真实后端接口
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

### 登录信息

开发环境 Mock 登录账号:
- 用户名: `admin`
- 密码: `admin`

---

## 项目结构

```
webui/
├── src/                          # 源代码目录
│   ├── models/                   # 实体类定义（独立的业务模型）
│   │   ├── common.model.ts       # 公共实体（Result, PageResult, Paging）
│   │   ├── auth.model.ts         # 认证相关实体
│   │   ├── book.model.ts         # 图书相关实体
│   │   ├── settings.model.ts     # 设置相关实体
│   │   └── index.ts              # 统一导出
│   ├── api/                      # API 接口定义（纯净的接口调用）
│   │   ├── auth.ts               # 认证 API
│   │   └── book.ts               # 图书 API
│   ├── components/               # 公共组件
│   │   ├── PrivateRoute.tsx      # 路由鉴权
│   │   ├── Toast.tsx             # 提示组件
│   │   ├── ToastContainer.tsx    # 提示容器
│   │   ├── ConfirmDialog.tsx     # 确认对话框
│   │   └── Logo.tsx              # Logo 组件
│   ├── pages/                    # 页面组件
│   │   ├── LoginPage.tsx         # 登录页
│   │   ├── RegisterPage.tsx      # 注册页
│   │   ├── HomePage.tsx          # 主页
│   │   ├── HistoryPage.tsx       # 历史记录页
│   │   ├── PreferencePage.tsx    # 用户偏好页
│   │   ├── ProfilePage.tsx       # 个人资料页
│   │   ├── ChangePasswordPage.tsx # 修改密码页
│   │   ├── ModelSettingsPage.tsx  # AI 模型设置页
│   │   ├── AIAnalysisSettingsPage.tsx # AI 分析设置页
│   │   └── AIImageSettingsPage.tsx    # AI 生图设置页
│   ├── utils/                    # 工具函数
│   │   └── request.ts            # Axios 封装（含拦截器）
│   ├── App.tsx                   # 根组件（路由配置）
│   ├── main.tsx                  # 入口文件
│   └── index.css                 # 全局样式
├── mock/                         # Mock 数据目录
│   ├── data/                     # Mock 数据源
│   │   ├── book.data.ts          # 图书 Mock 数据
│   │   └── user.data.ts          # 用户 Mock 数据
│   ├── auth.ts                   # 认证 Mock 接口
│   └── book.ts                   # 图书 Mock 接口
├── .env                          # 环境变量配置（真实接口）
├── .env.mock                     # Mock 模式配置
├── vite.config.ts                # Vite 配置（含 Mock 插件）
├── tailwind.config.js            # Tailwind 配置
└── package.json                  # 依赖配置
```

### 架构优势

#### 1. 实体与接口分离
- **实体类**（`models/`）：独立的业务模型,包含完整的类型定义
- **API 接口**（`api/`）：纯净的接口调用,只负责 HTTP 请求
- **Mock 数据**（`mock/`）：完全独立的 Mock 实现,不污染业务代码

#### 2. 数据与接口分离
- **数据源**（`mock/data/`）：独立的数据文件,便于维护和复用
- **Mock 接口**（`mock/*.ts`）：使用 vite-plugin-mock 标准格式,模拟真实 API 行为

#### 3. 统一的响应格式
- 所有接口返回都使用 `Result<T>` 包裹
- 分页接口使用 `Result<PageResult<T>>` 包裹
- 便于统一错误处理和数据提取

---

## Mock 系统

### 概述

项目使用 `vite-plugin-mock` 和 `mockjs` 实现了完整的 Mock 数据系统,实现了数据与接口的完全分离,可以方便地在 Mock 数据和真实接口之间切换。

### 切换 Mock 和真实接口

#### 方法一：使用 npm 命令（推荐）

```bash
# 使用 Mock 数据启动
npm run dev:mock

# 使用真实接口启动
npm run dev
```

#### 方法二：修改 `.env` 文件

编辑 `webui/.env` 文件中的 `VITE_USE_MOCK` 变量：

```bash
# 使用 Mock 数据
VITE_USE_MOCK=true

# 使用真实接口
VITE_USE_MOCK=false
VITE_API_BASE_URL=http://localhost:8080/api
```

#### 方法三：使用预配置文件

快速切换到 Mock 模式：
```bash
cp .env.mock .env
npm run dev
```

### Mock 功能特性

- ✅ 模拟网络延迟（timeout 配置）
- ✅ 支持动态响应（根据请求参数返回不同数据）
- ✅ 支持状态管理（内存存储模拟数据变化）
- ✅ 完整的错误模拟（401、403 等）
- ✅ 自动热更新（修改 mock 文件自动生效）

### 注意事项

1. **Mock 数据仅在开发环境生效**：生产构建会自动禁用 Mock
2. **保持数据结构一致**：Mock 数据应与真实 API 返回的数据结构保持一致
3. **测试两种模式**：开发时建议在 Mock 和真实接口两种模式下都测试功能
4. **环境变量修改后需重启**：修改 `.env` 文件后需要重启开发服务器

---

## API 接口文档

### 基础说明

#### 响应格式

所有接口统一使用 `Result<T>` 包裹返回数据：

```typescript
interface Result<T = any> {
  code: number;        // 状态码
  message: string;     // 提示信息
  ts: number;          // 时间戳
  data: T;             // 数据（泛型）
  success: boolean;    // 是否成功
  traceId?: string;    // 追踪ID（可选）
}
```

#### 分页格式

分页接口使用 `PageResult<T>` 结构：

```typescript
interface PageResult<T> {
  currentPage: number;  // 当前页码
  pageSize: number;     // 每页显示条数
  totalPages: number;   // 总页数
  total: number;        // 总记录数
  rows: T[];            // 数据列表
}
```

#### Token 处理

- Token 通过 Header 传递，key 为 `token`
- 前端会自动在请求拦截器中添加 token
- Token 失效返回 401 状态码，自动跳转登录页

---

## 1. 认证相关接口

### 1.1 用户登录

**接口地址:** `POST /api/login`

**请求参数:**
```typescript
{
  loginName: string;  // 用户名
  password: string;  // 密码
}
```

**响应数据:** `Result<TokenInfo>`
```typescript
Result<{
  token: string;          // JWT Token
  refreshToken?: string;  // 刷新Token（可选）
  expireTime?: number;    // 过期时间戳（可选）
}>
```

**Mock 示例:**
```typescript
// 成功
{
  code: 200,
  message: '登录成功',
  ts: 1234567890,
  data: {
    token: 'mock_token_1234567890',
    expireTime: 1234567890 + 86400000
  },
  success: true
}

// 失败
{
  code: 401,
  message: '用户名或密码错误',
  ts: 1234567890,
  data: null,
  success: false
}
```

---

### 1.2 用户登出

**接口地址:** `POST /api/logout`

**请求头:** `token: xxx`

**响应数据:** `Result<null>`

**Mock 示例:**
```typescript
{
  code: 200,
  message: '登出成功',
  ts: 1234567890,
  data: null,
  success: true
}
```

---

### 1.3 获取用户信息

**接口地址:** `GET /api/user/info`

**请求头:** `token: xxx`

**响应数据:** `Result<UserInfo>`
```typescript
Result<{
  username: string;     // 用户名
  nickname?: string;    // 昵称
  userId?: string;      // 用户ID
  email?: string;       // 邮箱
  avatar?: string;      // 头像URL
   createTime?: string;   // 注册时间
}>
```

**Mock 示例:**
```typescript
{
  code: 200,
  message: 'success',
  ts: 1234567890,
  data: {
    username: 'admin',
    nickname: '管理员',
    userId: '1'
  },
  success: true
}
```
---

## 2. 图书相关接口

### 2.1 获取快速推荐书籍

**接口地址:** `GET /api/book/recommend`

**请求头:** `token: xxx`（需要登录）

**响应数据:** `Result<string[]>`

**Mock 示例:**
```typescript
{
  code: 200,
  message: '获取成功',
  ts: 1234567890,
  data: ['三体', '活着', '解忧杂货店', '人类简史'],
  success: true
}
```

---

### 2.2 分析图书

**接口地址:** `POST /api/book/analyze`

**请求头:** `token: xxx`（需要登录）

**请求参数:**
```typescript
{
  title: string;  // 书名
}
```

**响应数据:** `Result<BookAnalysis>`
```typescript
Result<{
  bookId: number;
  summary: {
    title: string;              // 书名
    genre: string;              // 类型
    themes: string[];           // 主题列表
    tone: string;               // 基调
    keyElements: string[];      // 关键元素
    triggerWarnings: string[];  // 警告信息
  };
  posterUrl: string;            // 封面图URL
  recommendation: string;       // 推荐语
  showPoster: boolean;          // 是否显示封面
}>
```

**Mock 示例:**
```typescript
{
  code: 200,
  message: '分析完成',
  ts: 1234567890,
  data: {
    bookId: 1,
    summary: {
      title: '三体',
      genre: '科幻',
      themes: ['宇宙文明', '科技哲学', '人性探索', '生存困境'],
      tone: '深邃宏大',
      keyElements: ['黑暗森林法则', '三体文明', '降维打击', '宇宙社会学'],
      triggerWarnings: []
    },
    posterUrl: 'https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?w=800',
    recommendation: '基于你的阅读偏好，这本硬核科幻可能会让你着迷！',
    showPoster: true
  },
  success: true
}
```

---

### 2.3 提交反馈

**接口地址:** `POST /api/book/feedback`

**请求头:** `token: xxx`（需要登录）

**请求参数:**
```typescript
{
  bookId: number;       // 书籍ID
  interested: boolean;  // 是否感兴趣
  reason?: string;      // 原因（可选）
}
```

**响应数据:** `Result<null>`

**Mock 示例:**
```typescript
{
  code: 200,
  message: '反馈提交成功',
  ts: 1234567890,
  data: null,
  success: true
}
```

---

### 2.4 获取反馈历史

**接口地址:** `GET /api/book/feedback/history`

**请求头:** `token: xxx`（需要登录）

**响应数据:** `Result<FeedbackHistory[]>`
```typescript
Result<Array<{
  bookId: number;        // 书籍ID
  title: string;         // 书名
  interested: boolean;   // 是否感兴趣
  reason?: string;       // 原因
  timestamp: string;     // 时间戳（ISO格式）
}>>
```

---

### 2.5 获取分析历史（分页）

**接口地址:** `GET /api/book/history`

**请求头:** `token: xxx`（需要登录）

**请求参数:**
```typescript
{
  page: number;      // 页码
  pageSize: number;  // 每页数量
}
```

**响应数据:** `Result<PageResult<AnalysisHistory>>`
```typescript
Result<{
  currentPage: number;
  pageSize: number;
  totalPages: number;
  total: number;
  rows: Array<{
    id: string;
    bookId: number;
    title: string;
    interested: boolean;
    analysisData: BookAnalysis;
     createTime: string;
  }>;
}>
```

**Mock 示例:**
```typescript
{
  code: 200,
  message: '获取成功',
  ts: 1234567890,
  data: {
    currentPage: 1,
    pageSize: 10,
    totalPages: 1,
    total: 3,
    rows: [
      {
        id: '1',
        bookId: 1,
        title: '三体',
        interested: true,
        analysisData: { /* BookAnalysis 数据 */ },
         createTime: '2026-01-06T10:00:00.000Z'
      }
    ]
  },
  success: true
}
```

---

### 2.6 获取用户偏好

**接口地址:** `GET /api/user/preference`

**请求头:** `token: xxx`（需要登录）

**响应数据:** `Result<UserPreference>`
```typescript
Result<{
  summary: string;  // 偏好总结
  readingReport: {
    totalBooks: number;           // 总书籍数
    interestedBooks: number;      // 感兴趣的书籍数
    favoriteGenres: string[];     // 喜爱的类型
    favoriteThemes: string[];     // 喜爱的主题
    readingTrend: string;         // 阅读趋势
  };
  annualReport: {
    year: number;                 // 年份
    totalBooks: number;           // 年度总书籍数
    interestedCount: number;      // 年度感兴趣数
    topGenres: Array<{            // 热门类型
      genre: string;
      count: number;
    }>;
    topThemes: Array<{            // 热门主题
      theme: string;
      count: number;
    }>;
    monthlyTrend: Array<{         // 月度趋势
      month: number;
      count: number;
    }>;
    highlights: string[];         // 年度亮点
  };
}>
```

---

### 2.7 下载年度报告

**接口地址:** `GET /api/user/report/:year/download`

**请求头:** `token: xxx`（需要登录）

**路径参数:**
- `year`: 年份（如 2026）

**响应数据:** `Blob`（文件下载）

---

## 错误码说明

### HTTP 状态码

| HTTP Code | 业务 Code | 错误名称 | 说明 | 前端处理 |
|-----------|----------|---------|------|---------|
| 401 | TOKEN_ERROR | Token 校验失败 | Token 无效或解析失败 | 清除 token，跳转登录页 |
| 401 | REDIS_EXPIRED_USER | 登录失效 | Redis 中未找到对应登录信息 | 清除 token，跳转登录页 |
| 401 | REDIS_NO_USER | 非法登录 | Token 已被刷新或被其他登录覆盖 | 清除 token，跳转登录页 |
| 402 | SYS_AUTHORIZED_PAST | 授权过期 | 系统授权已过期 | 提示用户联系管理员 |
| 403 | UNAUTHENTICATED | 系统未授权 | 当前用户无访问权限 | 提示用户无权限 |
| 403 | UNAUTHENTICATED_PLATFORM | 非法令牌访问 | Token 与访问端不匹配 | 清除 token，跳转登录页 |

### 错误码定义

```typescript
export const AuthErrorCode = {
  TOKEN_ERROR: 'TOKEN_ERROR',
  REDIS_EXPIRED_USER: 'REDIS_EXPIRED_USER',
  REDIS_NO_USER: 'REDIS_NO_USER',
  SYS_AUTHORIZED_PAST: 'SYS_AUTHORIZED_PAST',
  UNAUTHENTICATED: 'UNAUTHENTICATED',
  UNAUTHENTICATED_PLATFORM: 'UNAUTHENTICATED_PLATFORM',
} as const;
```

### 全局错误处理

前端在 `utils/request.ts` 中实现了统一的错误拦截处理：

```typescript
// 响应拦截器 - 统一处理 Result 包裹的响应
instance.interceptors.response.use(
  (response) => {
    const data: Result = response.data;

    // 如果返回 success 为 false，视为业务错误
    if (data.success === false) {
      handleBusinessError(data);
      return Promise.reject(data);
    }

    return response;
  },
  (error: AxiosError<Result>) => {
    if (error.response) {
      const { status, data } = error.response;

      // 处理 HTTP 状态码错误
      if (status === 401) {
        handleAuthError(data);
      } else if (status === 403) {
        handleForbiddenError(data);
      } else if (status === 402) {
        handleExpiredError(data);
      } else {
        handleOtherError(status, data);
      }
    } else {
      // 网络错误
      console.error('网络错误:', error.message);
      alert('网络连接失败，请检查网络');
    }

    return Promise.reject(error);
  }
);
```

---

## 开发指南

### 添加新的 API 接口

#### 1. 定义实体类型（如果需要）

在 `src/models/` 中定义相关实体：

```typescript
// src/models/example.model.ts
export class Example {
  id: string;
  name: string;

  constructor(data: Partial<Example> = {}) {
    this.id = data.id || '';
    this.name = data.name || '';
  }
}
```

#### 2. 创建 API 接口

在 `src/api/` 中定义接口方法：

```typescript
// src/api/example.ts
import { request } from '../utils/request';
import type { Example, Result } from '../models';

export const exampleApi = {
  getList: () => {
    return request.get<Result<Example[]>>('/example/list');
  },

  create: (data: Example) => {
    return request.post<Result<Example>>('/example', data);
  },
};
```

#### 3. 创建 Mock 数据源（可选）

在 `mock/data/` 中创建数据文件：

```typescript
// mock/data/example.data.ts
export const mockExampleData = [
  { id: '1', name: 'Example 1' },
  { id: '2', name: 'Example 2' },
];
```

#### 4. 定义 Mock 接口（可选）

在 `mock/` 中创建 Mock 接口：

```typescript
// mock/example.ts
import { MockMethod } from 'vite-plugin-mock';
import { mockExampleData } from './data/example.data';
import type { Result, Example } from '../src/models';

export default [
  {
    url: '/api/example/list',
    method: 'get',
    timeout: 500,
    response: (): Result<Example[]> => {
      return {
        code: 200,
        message: '获取成功',
        ts: Date.now(),
        data: mockExampleData,
        success: true,
      };
    },
  },
] as MockMethod[];
```

### 使用 API 接口

在组件中使用定义好的 API：

```typescript
import { exampleApi } from '../api/example';

const loadData = async () => {
  try {
    const response = await exampleApi.getList();
    if (response.success) {
      console.log('数据:', response.data);
    } else {
      console.error('获取失败:', response.message);
    }
  } catch (error) {
    console.error('请求错误:', error);
  }
};
```

### JWT 认证机制

#### Token 存储
- 使用 `localStorage` 存储 token
- 请求拦截器自动添加到 header

```typescript
// 请求拦截器 - 添加 token
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers['token'] = token;
    }
    return config;
  }
);
```

#### 自动跳转登录

```typescript
// 清除认证信息并跳转登录
function clearAuthAndRedirect() {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');

  // 如果不在登录页则跳转
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}
```

### 开发建议

1. **代码规范**: 项目已配置 ESLint，建议开发时遵循
2. **类型安全**: 充分利用 TypeScript 的类型系统
3. **组件复用**: 将常用 UI 组件提取到 `components/`
4. **API 分层**: 保持 API 层的独立性，便于切换和测试
5. **错误处理**: 使用全局拦截器统一处理，避免重复代码
6. **响应时间**: Mock 接口建议控制在 200-500ms 内
7. **数据验证**: 前后端都应进行数据验证
8. **安全性**:
   - 密码需要加密存储
   - API Key 等敏感信息需要加密传输
   - 实现请求频率限制

### 常见问题

#### Q: 为什么修改 Mock 数据不生效？
A: 检查是否设置了 `VITE_USE_MOCK=true`，并重启开发服务器。

#### Q: 如何知道当前使用的是 Mock 还是真实接口？
A:
- Mock 模式下，Network 面板会看到请求被 vite-plugin-mock 拦截
- 控制台可以添加日志输出：`console.log('USE_MOCK:', import.meta.env.VITE_USE_MOCK)`

#### Q: Mock 数据和真实接口可以混用吗？
A: 不建议。应该完全使用 Mock 或完全使用真实接口，确保环境的一致性。

#### Q: 生产环境会包含 Mock 代码吗？
A: 不会。vite-plugin-mock 在生产构建时会自动禁用，Mock 代码不会被打包。

---

## 后续扩展

可以考虑添加的功能：
- [ ] 用户头像上传
- [ ] 找回密码
- [ ] 邮箱验证
- [ ] 书籍收藏功能
- [ ] 书籍评分
- [ ] 书评功能
- [ ] 主题切换（暗色模式）
- [ ] 国际化支持
- [ ] 单元测试
- [ ] E2E 测试

---

## 注意事项

1. 当前使用 Mock 数据，方便前端独立开发
2. 生产环境需要替换为真实 API
3. Token 存储在 localStorage，请注意安全性
4. 图片 URL 使用 Unsplash，可能需要替换为真实图床
5. 代理配置在 `vite.config.ts`，开发时转发到 `localhost:8080`

---

**更新时间:** 2026-01-06

**版本:** 1.0.0

**文档维护:** 前端开发团队
