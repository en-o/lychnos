# Mock API 接口文档

> 本文档列出前端当前使用的所有 Mock 接口,方便后端开发时参考实现

## 基础说明

### 响应格式

所有接口统一返回格式:

```typescript
{
  code: number;        // 状态码
  message: string;     // 提示信息
  ts: number;          // 时间戳
  data: T;             // 数据(泛型)
  success: boolean;    // 是否成功
  traceId?: string;    // 追踪ID(可选)
}
```

### Token 处理

- Token 通过 Header 传递,key 为 `token`
- 前端会自动在请求拦截器中添加 token
- Token 失效返回 401 状态码

---

## 1. 认证相关接口

### 1.1 用户登录

**接口地址:** `POST /api/auth/login`

**请求参数:**
```typescript
{
  username: string;  // 用户名
  password: string;  // 密码
}
```

**响应数据:**
```typescript
{
  token: string;          // JWT Token
  refreshToken?: string;  // 刷新Token(可选)
  expireTime?: number;    // 过期时间戳(可选)
}
```

**Mock 示例:**
```typescript
// 成功
{
  code: 200,
  message: '登录成功',
  ts: 1234567890,
  data: {
    token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
    expireTime: 1234567890
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

### 1.2 用户注册

**接口地址:** `POST /api/auth/register`

**请求参数:**
```typescript
{
  username: string;       // 用户名(必填,至少3字符)
  password: string;       // 密码(必填,至少6字符)
  nickname?: string;      // 昵称(可选)
  email?: string;         // 邮箱(可选)
}
```

**响应数据:**
```typescript
{
  userId: string;     // 用户ID
  username: string;   // 用户名
}
```

---

### 1.3 获取用户信息

**接口地址:** `GET /api/auth/userInfo`

**请求头:** `token: xxx`

**响应数据:**
```typescript
{
  username: string;     // 用户名
  nickname?: string;    // 昵称
  userId?: string;      // 用户ID
  email?: string;       // 邮箱
  avatar?: string;      // 头像URL
  createdAt?: string;   // 注册时间
}
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
    userId: '1',
    email: 'admin@example.com'
  },
  success: true
}
```

---

### 1.4 退出登录

**接口地址:** `POST /api/auth/logout`

**请求头:** `token: xxx`

**响应数据:** `null`

---

### 1.5 刷新Token

**接口地址:** `POST /api/auth/refresh`

**请求参数:**
```typescript
{
  refreshToken: string;  // 刷新Token
}
```

**响应数据:**
```typescript
{
  token: string;         // 新的JWT Token
  expireTime?: number;   // 过期时间
}
```

---

## 2. 图书相关接口

### 2.1 分析图书

**接口地址:** `POST /api/book/analyze`

**请求头:** `token: xxx` (需要登录)

**请求参数:**
```typescript
{
  title: string;  // 书名
}
```

**响应数据:**
```typescript
{
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
}
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
    recommendation: '基于你的阅读偏好,这本硬核科幻可能会让你着迷!',
    showPoster: true
  },
  success: true
}
```

---

### 2.2 提交反馈

**接口地址:** `POST /api/book/feedback`

**请求头:** `token: xxx` (需要登录)

**请求参数:**
```typescript
{
  bookId: number;       // 书籍ID
  interested: boolean;  // 是否感兴趣
  reason?: string;      // 原因(可选)
}
```

**响应数据:** `null`

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

### 2.3 获取反馈历史

**接口地址:** `GET /api/book/feedback/history`

**请求头:** `token: xxx` (需要登录)

**响应数据:**
```typescript
Array<{
  bookId: number;        // 书籍ID
  title: string;         // 书名
  interested: boolean;   // 是否感兴趣
  reason?: string;       // 原因
  timestamp: string;     // 时间戳(ISO格式)
}>
```

---

## 3. 用户资料接口

### 3.1 更新用户资料

**接口地址:** `PUT /api/user/profile`

**请求头:** `token: xxx`

**请求参数:**
```typescript
{
  nickname?: string;  // 昵称
  email?: string;     // 邮箱
  avatar?: string;    // 头像URL
}
```

**响应数据:**
```typescript
{
  username: string;
  nickname?: string;
  email?: string;
  avatar?: string;
}
```

---

### 3.2 修改密码

**接口地址:** `POST /api/user/password`

**请求头:** `token: xxx`

**请求参数:**
```typescript
{
  oldPassword: string;      // 当前密码
  newPassword: string;      // 新密码
  confirmPassword: string;  // 确认密码
}
```

**响应数据:** `null`

**注意:** 修改密码成功后,前端会清除 token 并跳转到登录页

---

## 4. AI模型设置接口

> 这些接口用于管理用户配置的 AI 模型,前端当前使用 localStorage 存储,后端可以实现为用户配置接口

### 4.1 获取AI分析模型列表

**接口地址:** `GET /api/settings/ai-analysis-models`

**请求头:** `token: xxx`

**响应数据:**
```typescript
Array<{
  id: string;
  name: string;                              // 模型名称
  type: 'openai' | 'ollama' | 'deepseek' | 'custom';  // 模型类型
  apiKey?: string;                           // API Key
  apiUrl?: string;                           // API URL
  model?: string;                            // 模型名称(如 gpt-4)
  enabled: boolean;                          // 是否启用
  isActive: boolean;                         // 是否为当前使用
}>
```

---

### 4.2 保存AI分析模型

**接口地址:** `POST /api/settings/ai-analysis-models`

**请求头:** `token: xxx`

**请求参数:**
```typescript
{
  name: string;
  type: 'openai' | 'ollama' | 'deepseek' | 'custom';
  apiKey?: string;
  apiUrl?: string;
  model?: string;
}
```

**响应数据:** 返回创建的模型对象

---

### 4.3 更新AI分析模型

**接口地址:** `PUT /api/settings/ai-analysis-models/:id`

**请求头:** `token: xxx`

**请求参数:** 同保存接口

---

### 4.4 删除AI分析模型

**接口地址:** `DELETE /api/settings/ai-analysis-models/:id`

**请求头:** `token: xxx`

---

### 4.5 设置当前使用的AI分析模型

**接口地址:** `POST /api/settings/ai-analysis-models/:id/activate`

**请求头:** `token: xxx`

---

### 4.6 AI生图模型接口

类似 AI分析模型,只是类型改为:
```typescript
type: 'stable-diffusion' | 'midjourney' | 'dall-e' | 'custom'
```

接口路径将 `ai-analysis-models` 替换为 `ai-image-models`

---

## 5. 错误码说明

| HTTP Code | 业务 Code | 错误名称 | 说明 |
|-----------|----------|---------|------|
| 401 | TOKEN_ERROR | Token 校验失败 | Token 无效或解析失败 |
| 401 | REDIS_EXPIRED_USER | 登录失效 | Redis 中未找到对应登录信息 |
| 401 | REDIS_NO_USER | 非法登录 | Token 已被刷新或被其他登录覆盖 |
| 402 | SYS_AUTHORIZED_PAST | 授权过期 | 系统授权已过期 |
| 403 | UNAUTHENTICATED | 系统未授权 | 当前用户无访问权限 |
| 403 | UNAUTHENTICATED_PLATFORM | 非法令牌访问 | Token 与访问端不匹配 |

---

## 6. 前端Mock文件位置

- **认证API:** `src/api/auth.ts` - `mockAuthApi`
- **图书API:** `src/api/book.ts` - `mockBookApi`

---

## 7. 接口迁移指南

### 7.1 切换到真实API

1. 更新 `.env` 文件中的 `VITE_API_BASE_URL`
2. 在需要的页面中,将 `mockAuthApi` 替换为 `authApi`
3. 将 `mockBookApi` 替换为 `bookApi`

### 7.2 示例

```typescript
// Mock版本
import { mockAuthApi } from '../api/auth';
const response = await mockAuthApi.login({ username, password });

// 真实API版本
import { authApi } from '../api/auth';
const response = await authApi.login({ username, password });
```

---

## 8. 开发建议

1. **响应时间:** 建议控制在 200-500ms 内
2. **分页:** 如果反馈历史数据较多,建议实现分页
3. **文件上传:** 头像上传可以单独实现文件上传接口
4. **数据验证:** 建议后端也进行数据验证(用户名长度、邮箱格式等)
5. **安全性:**
   - 密码需要加密存储
   - API Key 等敏感信息需要加密传输
   - 实现请求频率限制

---

## 9. 待实现功能

这些功能前端暂未实现,但后端可以预留接口:

- 找回密码
- 邮箱验证
- 用户头像上传
- 阅读历史
- 书籍收藏
- 书籍评分
- 书评功能

---

更新时间: 2026-01-05
