package cn.tannn.lychnos.ai.factory;

import cn.tannn.lychnos.ai.config.DynamicAIModelConfig;
import cn.tannn.lychnos.ai.modelscope.ModelScopeImageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


/**
 * 动态 AI 客户端工厂
 * 根据配置动态创建 OpenAI 兼容的 AI 客户端
 * <p>
 * 支持的厂商：
 * <ul>
 *   <li>文本分析模型: OpenAI, Ollama, DeepSeek, Azure OpenAI, Anthropic, 通义千问, 百度文心, 魔搭社区, Hugging Face</li>
 *   <li>图片生成模型: 魔搭社区（异步调用）</li>
 *   <li>任何兼容 OpenAI API 协议的厂商</li>
 * </ul>
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
@Slf4j
@Component
public class DynamicAIClientFactory {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 创建文本聊天模型
     *
     * @param config 动态配置
     * @return ChatModel
     */
    public ChatModel createChatModel(DynamicAIModelConfig config) {
        log.info("创建文本聊天模型，baseUrl: {}, model: {}", config.getBaseUrl(), config.getModel());

        // 创建 OpenAI API 客户端
        OpenAiApi openAiApi = createOpenAiApi(config);

        // 创建 OpenAI 聊天模型
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(org.springframework.ai.openai.OpenAiChatOptions.builder()
                        .model(config.getModel())
                        .temperature(config.getTemperature())
                        .maxTokens(config.getMaxTokens())
                        .build())
                .build();
    }

    /**
     * 创建图片生成模型
     *
     * @param config 动态配置
     * @param factory 厂商标识（modelscope-image 等）
     * @return ImageModel
     */
    public ImageModel createImageModel(DynamicAIModelConfig config, String factory) {
        log.info("创建图片生成模型，factory: {}, baseUrl: {}, model: {}", factory, config.getBaseUrl(), config.getModel());

        // 魔搭社区使用自定义异步客户端
        if ("modelscope-image".equalsIgnoreCase(factory)) {
            return createModelScopeImageModel(config);
        }

        // 其他厂商使用 OpenAI 兼容客户端
        return createOpenAiImageModel(config);
    }

    /**
     * 创建 ModelScope 图片生成模型（异步）
     */
    private ImageModel createModelScopeImageModel(DynamicAIModelConfig config) {
        String apiKey = StringUtils.hasText(config.getApiKey()) ? config.getApiKey() : "dummy";
        return new ModelScopeImageModel(
                apiKey,
                config.getBaseUrl(),
                config.getModel(),
                restTemplate
        );
    }

    /**
     * 创建 OpenAI 兼容的图片生成模型
     */
    private ImageModel createOpenAiImageModel(DynamicAIModelConfig config) {
        // 创建 OpenAI Image API 客户端
        OpenAiImageApi openAiImageApi = createOpenAiImageApi(config);

        // 创建图片选项
        OpenAiImageOptions imageOptions = OpenAiImageOptions.builder()
                .model(config.getModel())
                .build();

        // 创建 OpenAI 图片模型（在构造函数中传入选项）
        return new OpenAiImageModel(openAiImageApi, imageOptions, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    /**
     * 创建 OpenAI API 客户端（用于文本聊天）
     */
    private OpenAiApi createOpenAiApi(DynamicAIModelConfig config) {
        String apiKey = StringUtils.hasText(config.getApiKey()) ? config.getApiKey() : "dummy";
        String baseUrl = config.getBaseUrl();

        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
    }

    /**
     * 创建 OpenAI Image API 客户端（用于图片生成）
     */
    private OpenAiImageApi createOpenAiImageApi(DynamicAIModelConfig config) {
        String apiKey = StringUtils.hasText(config.getApiKey()) ? config.getApiKey() : "dummy";
        String baseUrl = config.getBaseUrl();

        return OpenAiImageApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
    }
}
