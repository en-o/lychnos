package cn.tannn.lychnos.ai.client;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.image.ImageResponse;

/**
 * 动态 AI 客户端接口
 * 参考 Spring AI ChatClient 设计，提供流式 API 调用方式
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/26
 */
public interface DynamicAIClient {

    /**
     * 创建文本提示构建器
     *
     * @return TextPromptBuilder
     */
    TextPromptBuilder prompt();

    /**
     * 创建图片提示构建器
     *
     * @return ImagePromptBuilder
     */
    ImagePromptBuilder imagePrompt();

    /**
     * 文本提示构建器
     */
    interface TextPromptBuilder {
        /**
         * 设置用户消息
         *
         * @param userMessage 用户消息
         * @return this
         */
        TextPromptBuilder user(String userMessage);

        /**
         * 设置系统消息
         *
         * @param systemMessage 系统消息
         * @return this
         */
        TextPromptBuilder system(String systemMessage);

        /**
         * 设置温度参数（覆盖默认值）
         *
         * @param temperature 温度值 (0.0-1.0)
         * @return this
         */
        TextPromptBuilder temperature(Double temperature);

        /**
         * 设置最大 Token 数（覆盖默认值）
         *
         * @param maxTokens 最大 Token 数
         * @return this
         */
        TextPromptBuilder maxTokens(Integer maxTokens);

        /**
         * 调用 AI 模型并返回响应
         *
         * @return ChatResponse
         */
        ChatResponse call();

        /**
         * 调用 AI 模型并直接返回文本内容
         *
         * @return 文本内容
         */
        String content();
    }

    /**
     * 图片提示构建器
     */
    interface ImagePromptBuilder {
        /**
         * 设置提示词
         *
         * @param prompt 提示词
         * @return this
         */
        ImagePromptBuilder prompt(String prompt);

        /**
         * 设置图片宽度（覆盖默认值）
         *
         * @param width 宽度
         * @return this
         */
        ImagePromptBuilder width(Integer width);

        /**
         * 设置图片高度（覆盖默认值）
         *
         * @param height 高度
         * @return this
         */
        ImagePromptBuilder height(Integer height);

        /**
         * 调用 AI 模型并返回响应
         *
         * @return ImageResponse
         */
        ImageResponse call();

        /**
         * 调用 AI 模型并直接返回图片 URL
         *
         * @return 图片 URL
         */
        String url();
    }
}
