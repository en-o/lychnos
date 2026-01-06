# Mock数据使用说明

## 概述

项目已经使用 `vite-plugin-mock` 和 `mockjs` 重构了Mock数据系统，实现了数据与接口的完全分离，可以方便地在Mock数据和真实接口之间切换。

## 目录结构

```
webui/
├── src/
│   ├── models/              # 实体类定义（独立的业务模型）
│   │   ├── auth.model.ts    # 认证相关实体
│   │   ├── book.model.ts    # 图书相关实体
│   │   ├── settings.model.ts # 设置相关实体
│   │   └── index.ts         # 统一导出
│   ├── api/                 # API接口定义（纯净的接口调用）
│   │   ├── auth.ts          # 认证API
│   │   └── book.ts          # 图书API
│   └── types/               # 保留的类型定义（向后兼容）
├── mock/                    # Mock数据目录
│   ├── data/                # Mock数据源
│   │   ├── book.data.ts     # 图书Mock数据
│   │   └── user.data.ts     # 用户Mock数据
│   ├── auth.ts              # 认证Mock接口
│   └── book.ts              # 图书Mock接口
└── .env                     # 环境变量配置
```

## 架构优势

### 1. 实体与接口分离
- **实体类**（`models/`）：独立的业务模型，使用class定义，包含构造函数和默认值
- **API接口**（`api/`）：纯净的接口调用，只负责HTTP请求
- **Mock数据**（`mock/`）：完全独立的Mock实现，不污染业务代码

### 2. 数据与接口分离
- **数据源**（`mock/data/`）：独立的数据文件，便于维护和复用
- **Mock接口**（`mock/*.ts`）：使用vite-plugin-mock标准格式，模拟真实API行为

### 3. 便捷的切换机制
通过环境变量一键切换Mock和真实接口，无需修改代码

## 如何切换Mock和真实接口

### 方法一：修改 `.env` 文件

编辑 `webui/.env` 文件中的 `VITE_USE_MOCK` 变量：

```bash
# 使用Mock数据
VITE_USE_MOCK=true

# 使用真实接口
VITE_USE_MOCK=false
```

### 方法二：使用预配置文件

快速切换到Mock模式：
```bash
cp .env.mock .env
npm run dev
```

恢复真实接口模式：
```bash
# 修改.env文件
VITE_USE_MOCK=false
```

### 方法三：环境变量启动

```bash
# 使用Mock启动
VITE_USE_MOCK=true npm run dev

# 使用真实接口启动
VITE_USE_MOCK=false npm run dev
```

## Mock数据开发指南

### 添加新的Mock接口

1. **定义实体类**（如果需要）
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

2. **创建Mock数据源**
```typescript
// mock/data/example.data.ts
export const mockExampleData = {
  item1: { id: '1', name: 'Example 1' },
  item2: { id: '2', name: 'Example 2' },
};
```

3. **定义Mock接口**
```typescript
// mock/example.ts
import { MockMethod } from 'vite-plugin-mock';
import { mockExampleData } from './data/example.data';

export default [
  {
    url: '/api/example',
    method: 'get',
    timeout: 500,
    response: () => {
      return {
        code: 200,
        message: 'success',
        ts: Date.now(),
        data: mockExampleData,
        success: true,
      };
    },
  },
] as MockMethod[];
```

4. **定义真实API接口**
```typescript
// src/api/example.ts
import { request } from '../utils/request';
import type { Example } from '../models';

export const exampleApi = {
  getList: () => {
    return request.get<Example[]>('/example');
  },
};
```

## 注意事项

1. **Mock数据仅在开发环境生效**：生产构建会自动禁用Mock
2. **保持数据结构一致**：Mock数据应与真实API返回的数据结构保持一致
3. **测试两种模式**：开发时建议在Mock和真实接口两种模式下都测试功能
4. **环境变量修改后需重启**：修改`.env`文件后需要重启开发服务器

## Mock功能特性

### 当前Mock支持的接口

#### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/userInfo` - 获取用户信息
- `POST /api/auth/refresh` - 刷新Token

#### 图书相关
- `POST /api/book/analyze` - 分析图书
- `POST /api/book/feedback` - 提交反馈
- `GET /api/book/feedback/history` - 获取反馈历史
- `GET /api/book/history` - 获取分析历史（分页）
- `GET /api/user/preference` - 获取用户偏好
- `GET /api/user/report/:year/download` - 下载年度报告

### Mock特性
- ✅ 模拟网络延迟（timeout配置）
- ✅ 支持动态响应（根据请求参数返回不同数据）
- ✅ 支持状态管理（内存存储模拟数据变化）
- ✅ 完整的错误模拟（401、403等）
- ✅ 自动热更新（修改mock文件自动生效）

## 常见问题

### Q: 为什么修改Mock数据不生效？
A: 检查是否设置了 `VITE_USE_MOCK=true`，并重启开发服务器。

### Q: 如何知道当前使用的是Mock还是真实接口？
A:
- Mock模式下，Network面板会看到请求被vite-plugin-mock拦截
- 控制台可以添加日志输出 `console.log('USE_MOCK:', import.meta.env.VITE_USE_MOCK)`

### Q: Mock数据和真实接口可以混用吗？
A: 不建议。应该完全使用Mock或完全使用真实接口，确保环境的一致性。

### Q: 生产环境会包含Mock代码吗？
A: 不会。vite-plugin-mock在生产构建时会自动禁用，Mock代码不会被打包。

## 迁移说明

原有的 `mockAuthApi` 和 `mockBookApi` 已被移除，所有Mock逻辑已迁移到 `mock/` 目录。旧代码的功能已完全保留，只是实现方式更加规范。

如果您的代码中直接引用了 `mockAuthApi` 或 `mockBookApi`，请修改为统一使用 `authApi` 和 `bookApi`，并通过环境变量控制Mock/真实接口切换。
