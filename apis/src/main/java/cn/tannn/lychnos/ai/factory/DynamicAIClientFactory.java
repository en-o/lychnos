package cn.tannn.lychnos.ai.factory;

import cn.tannn.lychnos.ai.config.DynamicAIModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 动态 AI 客户端工厂
 * 根据配置动态创建 OpenAI 兼容的 AI 客户端
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
@Slf4j
@Component
public class DynamicAIClientFactory {

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
     * @return ImageModel
     */
    public ImageModel createImageModel(DynamicAIModelConfig config) {
        log.info("创建图片生成模型，baseUrl: {}, model: {}", config.getBaseUrl(), config.getModel());

        // 创建 OpenAI Image API 客户端
        OpenAiImageApi openAiImageApi = createOpenAiImageApi(config);

        // 创建 OpenAI 图片模型
        return OpenAiImageModel.builder()
                .openAiImageApi(openAiImageApi)
                .defaultOptions(org.springframework.ai.openai.OpenAiImageOptions.builder()
                        .model(config.getModel())
                        .build())
                .build();
    }

    /**
     * 创建 OpenAI API 客户端（用于文本聊天）
     */
    private OpenAiApi createOpenAiApi(DynamicAIModelConfig config) {
        String apiKey = StringUtils.hasText(config.getApiKey()) ? config.getApiKey() : "dummy";
        String baseUrl = config.getBaseUrl();

        return new OpenAiApi(baseUrl, apiKey, null, null);
    }

    /**
     * 创建 OpenAI Image API 客户端（用于图片生成）
     */
    private OpenAiImageApi createOpenAiImageApi(DynamicAIModelConfig config) {
        String apiKey = StringUtils.hasText(config.getApiKey()) ? config.getApiKey() : "dummy";
        String baseUrl = config.getBaseUrl();

        return new OpenAiImageApi(baseUrl, apiKey, null);
    }
}
