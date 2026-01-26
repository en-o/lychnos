# DynamicAIClient 使用指南

## 概述

`DynamicAIClient` 是参考 Spring AI `ChatClient` 设计的流式 API 客户端，提供更优雅的参数传递和链式调用方式。

## 设计优势

### 1. 旧方式的问题

```java
// 旧方式：直接使用 ChatModel 和 ImageModel
ChatModel chatModel = clientFactory.createChatModel(aiModel);
ChatResponse response = chatModel.call(new Prompt(prompt));

ImageModel imageModel = clientFactory.createImageModel(aiModel);
ImageResponse response = imageModel.call(new ImagePrompt(prompt));
```

**存在的问题**：
- 每次调用都需要手动创建 Model 和 Prompt
- 参数在创建时固定，无法在运行时动态调整
- 缺少链式调用的便利性
- 代码冗长，不够优雅

### 2. 新方式的优势

```java
// 新方式：使用 DynamicAIClient
DynamicAIClient client = clientFactory.createClient(aiModel);

// 文本生成 - 链式调用
String result = client.prompt()
    .user("你好，请介绍一下 Spring AI")
    .temperature(0.8)
    .content();

// 图片生成 - 链式调用
String imageUrl = client.imagePrompt()
    .prompt("一只可爱的猫咪")
    .width(1024)
    .height(1024)
    .url();
```

**优势**：
- ✅ 链式调用，代码更简洁优雅
- ✅ 运行时参数覆盖（temperature、maxTokens、width、height）
- ✅ 默认参数和运行时参数分离
- ✅ 类型安全的 Builder 模式
- ✅ 参考 Spring AI 官方设计，符合最佳实践

## 详细使用示例

### 1. 文本生成

#### 基础用法

```java
@Service
@RequiredArgsConstructor
public class MyService {

    private final DynamicAIClientFactory clientFactory;
    private final AIModelService aiModelService;

    public String generateText(Long userId, String userMessage) {
        // 获取用户的 AI 模型配置
        AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.TEXT);

        // 创建客户端
        DynamicAIClient client = clientFactory.createClient(aiModel);

        // 调用 AI 生成文本（使用默认参数）
        String result = client.prompt()
            .user(userMessage)
            .content();

        return result;
    }
}
```

#### 高级用法 - 运行时参数覆盖

```java
public String generateCreativeText(Long userId, String userMessage) {
    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.TEXT);
    DynamicAIClient client = clientFactory.createClient(aiModel);

    // 覆盖默认的 temperature 和 maxTokens
    String result = client.prompt()
        .user(userMessage)
        .temperature(0.9)      // 更高的创造性
        .maxTokens(2000)       // 更长的输出
        .content();

    return result;
}
```

#### 带系统消息的用法

```java
public String generateWithSystemPrompt(Long userId, String userMessage) {
    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.TEXT);
    DynamicAIClient client = clientFactory.createClient(aiModel);

    // 设置系统消息和用户消息
    String result = client.prompt()
        .system("你是一个专业的技术文档撰写助手，擅长用简洁清晰的语言解释复杂的技术概念。")
        .user(userMessage)
        .temperature(0.7)
        .content();

    return result;
}
```

#### 获取完整响应对象

```java
public ChatResponse generateWithFullResponse(Long userId, String userMessage) {
    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.TEXT);
    DynamicAIClient client = clientFactory.createClient(aiModel);

    // 获取完整的 ChatResponse（包含元数据、token 使用量等）
    ChatResponse response = client.prompt()
        .user(userMessage)
        .call();

    // 可以访问更多信息
    String content = response.getResult().getOutput().getText();
    var metadata = response.getMetadata();

    return response;
}
```

### 2. 图片生成

#### 基础用法

```java
public String generateImage(Long userId, String prompt) {
    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.IMAGE);
    DynamicAIClient client = clientFactory.createClient(aiModel);

    // 生成图片并直接获取 URL（使用默认尺寸 1920x1080）
    String imageUrl = client.imagePrompt()
        .prompt(prompt)
        .url();

    return imageUrl;
}
```

#### 自定义尺寸

```java
public String generateCustomSizeImage(Long userId, String prompt) {
    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.IMAGE);
    DynamicAIClient client = clientFactory.createClient(aiModel);

    // 覆盖默认尺寸
    String imageUrl = client.imagePrompt()
        .prompt(prompt)
        .width(1024)
        .height(1024)
        .url();

    return imageUrl;
}
```

#### 获取完整响应对象

```java
public ImageResponse generateImageWithFullResponse(Long userId, String prompt) {
    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.IMAGE);
    DynamicAIClient client = clientFactory.createClient(aiModel);

    // 获取完整的 ImageResponse
    ImageResponse response = client.imagePrompt()
        .prompt(prompt)
        .width(1920)
        .height(1080)
        .call();

    // 可以访问更多信息
    String url = response.getResult().getOutput().getUrl();
    var metadata = response.getMetadata();

    return response;
}
```

### 3. 在 AIServiceImpl 中的应用示例

```java
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final DynamicAIClientFactory clientFactory;
    private final AIModelService aiModelService;

    @Override
    public String generateText(Long userId, String prompt) {
        validateUserId(userId);
        validatePrompt(prompt);

        // 获取用户启用的默认模型
        AIModel aiModel = getEnabledModel(userId, ModelType.TEXT);

        // 使用新的客户端方式
        DynamicAIClient client = clientFactory.createClient(aiModel);

        try {
            log.info("调用AI文本生成，modelId: {}, userId: {}, model: {}",
                    aiModel.getId(), aiModel.getUserId(), aiModel.getModel());

            // 链式调用生成文本
            String result = client.prompt()
                .user(prompt)
                .content();

            log.info("AI文本生成成功，modelId: {}, 响应长度: {}",
                    aiModel.getId(), result.length());

            return result;
        } catch (Exception e) {
            log.error("AI文本生成失败，modelId: {}, userId: {}, error: {}",
                    aiModel.getId(), aiModel.getUserId(), e.getMessage(), e);
            throw new AIException.ModelCallFailedException("文本生成失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ImageResponse generateImage(Long userId, String prompt) {
        validateUserId(userId);
        validatePrompt(prompt);

        // 获取用户启用的默认模型
        AIModel aiModel = getEnabledModel(userId, ModelType.IMAGE);

        // 使用新的客户端方式
        DynamicAIClient client = clientFactory.createClient(aiModel);

        try {
            log.info("调用AI图片生成，modelId: {}, userId: {}, model: {}",
                    aiModel.getId(), aiModel.getUserId(), aiModel.getModel());

            // 压缩提示词
            String compressedPrompt = ZipUtil.smartCompressPrompt(prompt, 1999);

            // 链式调用生成图片
            ImageResponse response = client.imagePrompt()
                .prompt(compressedPrompt)
                .call();

            log.info("AI图片生成成功，modelId: {}", aiModel.getId());
            return response;
        } catch (Exception e) {
            log.error("AI图片生成失败，modelId: {}, userId: {}, error: {}",
                    aiModel.getId(), aiModel.getUserId(), e.getMessage(), e);
            throw new AIException.ModelCallFailedException("图片生成失败: " + e.getMessage(), e);
        }
    }
}
```

## API 参考

### DynamicAIClient 接口

#### 文本提示构建器 (TextPromptBuilder)

| 方法 | 参数 | 说明 | 返回值 |
|------|------|------|--------|
| `user(String)` | 用户消息 | 设置用户消息 | TextPromptBuilder |
| `system(String)` | 系统消息 | 设置系统消息（可选） | TextPromptBuilder |
| `temperature(Double)` | 温度值 (0.0-1.0) | 覆盖默认温度参数 | TextPromptBuilder |
| `maxTokens(Integer)` | 最大 Token 数 | 覆盖默认最大 Token 数 | TextPromptBuilder |
| `call()` | - | 调用 AI 并返回完整响应 | ChatResponse |
| `content()` | - | 调用 AI 并直接返回文本内容 | String |

#### 图片提示构建器 (ImagePromptBuilder)

| 方法 | 参数 | 说明 | 返回值 |
|------|------|------|--------|
| `prompt(String)` | 提示词 | 设置图片生成提示词 | ImagePromptBuilder |
| `width(Integer)` | 宽度 | 覆盖默认图片宽度 | ImagePromptBuilder |
| `height(Integer)` | 高度 | 覆盖默认图片高度 | ImagePromptBuilder |
| `call()` | - | 调用 AI 并返回完整响应 | ImageResponse |
| `url()` | - | 调用 AI 并直接返回图片 URL | String |

## 参数覆盖机制

### 默认参数 vs 运行时参数

```java
// 创建客户端时设置默认参数（来自 AIModel 配置）
DynamicAIClient client = clientFactory.createClient(aiModel);
// 默认参数：temperature=0.7, maxTokens=1000, width=1920, height=1080

// 运行时覆盖部分参数
String result = client.prompt()
    .user("你好")
    .temperature(0.9)  // 覆盖默认的 0.7
    // maxTokens 使用默认的 1000
    .content();
```

### 参数优先级

1. **运行时参数**（最高优先级）：通过 Builder 方法设置的参数
2. **默认参数**（次优先级）：创建客户端时从 AIModel 配置读取的参数
3. **模型默认值**（最低优先级）：AI 模型提供商的默认值

## 最佳实践

### 1. 客户端复用

```java
// ❌ 不推荐：每次调用都创建新客户端
public String generate1(Long userId, String prompt) {
    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.TEXT);
    DynamicAIClient client = clientFactory.createClient(aiModel);
    return client.prompt().user(prompt).content();
}

public String generate2(Long userId, String prompt) {
    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.TEXT);
    DynamicAIClient client = clientFactory.createClient(aiModel);  // 重复创建
    return client.prompt().user(prompt).content();
}

// ✅ 推荐：在同一个请求中复用客户端
public Map<String, String> generateMultiple(Long userId, List<String> prompts) {
    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.TEXT);
    DynamicAIClient client = clientFactory.createClient(aiModel);  // 只创建一次

    Map<String, String> results = new HashMap<>();
    for (String prompt : prompts) {
        String result = client.prompt().user(prompt).content();
        results.put(prompt, result);
    }
    return results;
}
```

### 2. 异常处理

```java
public String generateWithErrorHandling(Long userId, String prompt) {
    try {
        AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.TEXT);
        DynamicAIClient client = clientFactory.createClient(aiModel);

        return client.prompt()
            .user(prompt)
            .content();

    } catch (AIException.ModelCallFailedException e) {
        log.error("AI 调用失败: {}", e.getMessage());
        throw e;
    } catch (Exception e) {
        log.error("未知错误: {}", e.getMessage());
        throw new AIException.ModelCallFailedException("生成失败", e);
    }
}
```

### 3. 参数验证

```java
public String generateWithValidation(Long userId, String prompt, Double temperature) {
    // 验证参数
    if (temperature != null && (temperature < 0.0 || temperature > 1.0)) {
        throw new IllegalArgumentException("temperature 必须在 0.0 到 1.0 之间");
    }

    AIModel aiModel = aiModelService.getEnabledModel(userId, ModelType.TEXT);
    DynamicAIClient client = clientFactory.createClient(aiModel);

    var builder = client.prompt().user(prompt);

    // 条件性设置参数
    if (temperature != null) {
        builder.temperature(temperature);
    }

    return builder.content();
}
```

## 迁移指南

### 从旧方式迁移到新方式

#### 文本生成迁移

```java
// 旧方式
ChatModel chatModel = clientFactory.createChatModel(aiModel);
ChatResponse response = chatModel.call(new Prompt(prompt));
String result = response.getResults().get(0).getOutput().getText();

// 新方式
DynamicAIClient client = clientFactory.createClient(aiModel);
String result = client.prompt().user(prompt).content();
```

#### 图片生成迁移

```java
// 旧方式
ImageModel imageModel = clientFactory.createImageModel(aiModel);
ImageResponse response = imageModel.call(new ImagePrompt(prompt));
String url = response.getResult().getOutput().getUrl();

// 新方式
DynamicAIClient client = clientFactory.createClient(aiModel);
String url = client.imagePrompt().prompt(prompt).url();
```

## 总结

`DynamicAIClient` 提供了一种更优雅、更灵活的方式来调用 AI 模型：

1. **链式调用**：代码更简洁、可读性更强
2. **参数灵活性**：支持默认参数和运行时参数覆盖
3. **类型安全**：Builder 模式提供编译时类型检查
4. **符合最佳实践**：参考 Spring AI 官方 ChatClient 设计

推荐在新代码中使用 `DynamicAIClient`，旧代码可以逐步迁移。

## 参考资料

- [Spring AI ChatClient 官方文档](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Spring AI 项目地址](https://github.com/spring-projects/spring-ai)
