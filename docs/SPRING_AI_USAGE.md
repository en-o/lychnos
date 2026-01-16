# Spring AI 动态模型调用使用说明

## 概述

本项目已集成 Spring AI 1.1.2，支持基于 `AIModel` 实体的动态 AI 模型调用。所有支持 OpenAI 协议的 AI 厂家都可以通过配置使用。

## 支持的 AI 厂家

以下厂家都支持 OpenAI 兼容协议：

- OpenAI (GPT-3.5, GPT-4, DALL-E等)
- DeepSeek
- Ollama (本地部署)
- Azure OpenAI
- 阿里千问 (Qwen)
- 百度文心一言
- 其他任何支持 OpenAI 协议的厂家

## 架构说明

### 核心组件

1. **DynamicAIModelConfig** - 动态 AI 模型配置类
   - 封装了 API Key、Base URL、模型名称等配置信息

2. **DynamicAIClientFactory** - 动态 AI 客户端工厂
   - 根据 `AIModel` 配置动态创建 ChatModel 或 ImageModel
   - 支持文本生成和图片生成

3. **AIService** - AI 服务接口
   - 提供统一的 AI 调用能力
   - 支持使用用户默认启用的模型或指定模型ID

4. **AIServiceImpl** - AI 服务实现
   - 自动从数据库查询用户配置的模型
   - 动态创建并调用 AI 客户端

5. **AIException** - AI 异常处理
   - 提供细粒度的异常类型
   - 便于问题定位和用户友好的错误提示


## 配置说明

### AIModel 字段说明

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| userId | Long | 用户ID | 1 |
| name | String | 配置名称 | "DeepSeek Chat" |
| model | String | 模型名称 | "deepseek-chat" |
| factory | String | 厂家标识（展示用） | "deepseek" |
| apiKey | String | API密钥（可选） | "sk-xxx" |
| apiUrl | String | API地址 | "https://api.deepseek.com" |
| enabled | Boolean | 是否启用 | true |
| type | ModelType | 模型类型 | TEXT / IMAGE |

### 不同厂家的配置示例

#### OpenAI

```sql
INSERT INTO tb_ai_model (user_id, name, model, factory, api_key, api_url, enabled, type)
VALUES (1, 'GPT-4', 'gpt-4', 'openai', 'sk-xxxxx', 'https://api.openai.com', 1, 'TEXT');
```

#### DeepSeek

```sql
INSERT INTO tb_ai_model (user_id, name, model, factory, api_key, api_url, enabled, type)
VALUES (1, 'DeepSeek Chat', 'deepseek-chat', 'deepseek', 'sk-xxxxx', 'https://api.deepseek.com', 1, 'TEXT');
```

#### Ollama（本地）

```sql
INSERT INTO tb_ai_model (user_id, name, model, factory, api_key, api_url, enabled, type)
VALUES (1, 'Llama 3.1 Local', 'llama3.1', 'ollama', NULL, 'http://localhost:11434', 1, 'TEXT');
```

#### 阿里千问

```sql
INSERT INTO tb_ai_model (user_id, name, model, factory, api_key, api_url, enabled, type)
VALUES (1, 'Qwen Turbo', 'qwen-turbo', 'qwen', 'sk-xxxxx', 'https://dashscope.aliyuncs.com/compatible-mode', 1, 'TEXT');
```

#### 魔搭社区图片生成

```sql
-- Z-Image-Turbo 图片生成模型（推荐）
INSERT INTO tb_ai_model (user_id, name, model, factory, api_key, api_url, enabled, type)
VALUES (1, 'Z-Image-Turbo', 'Tongyi-MAI/Z-Image-Turbo', 'modelscope', 'your-sdk-token', 'https://api-inference.modelscope.cn', 1, 'IMAGE');

-- 其他魔搭图片生成模型
INSERT INTO tb_ai_model (user_id, name, model, factory, api_key, api_url, enabled, type)
VALUES (1, 'MYkawaii4MJ', 'ChaosMY/MYkawaii4MJ', 'modelscope', 'your-sdk-token', 'https://api-inference.modelscope.cn', 1, 'IMAGE');
```

## 异常处理

系统提供了以下异常类型：

- `ModelNotConfiguredException` - 模型未配置
- `ModelNotEnabledException` - 模型未启用
- `ModelCallFailedException` - AI 调用失败
- `ResponseParseException` - 响应解析失败

所有异常都会被记录到日志，并向用户返回友好的错误提示。

## 注意事项

1. **API Key 安全性**：请确保不要将 API Key 提交到版本控制系统
2. **模型启用规则**：每个用户每种类型的模型只能启用一个
3. **API URL 格式**：必须是完整的 URL，包括协议和路径（通常以 `/v1` 结尾）
4. **Ollama 本地部署**：API Key 可以为空，URL 指向本地服务地址
5. **成本控制**：建议在配置中设置合理的 maxTokens 和 temperature 参数

## 扩展开发

如果需要支持更多功能，可以扩展以下内容：

1. 在 `DynamicAIModelConfig` 中添加更多配置参数
2. 在 `AIService` 中添加更多 AI 能力（如流式输出、函数调用等）
3. 为不同的业务场景创建专门的 Service（参考 `BookAnalyseService`）

## 参考资料

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API 文档](https://platform.openai.com/docs/api-reference)
- [DeepSeek API 文档](https://platform.deepseek.com/docs)
