package cn.tannn.lychnos.ai.client;

import cn.tannn.lychnos.ai.exception.AIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认动态 AI 客户端实现
 * 参考 Spring AI ChatClient 设计
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/26
 */
@Slf4j
public class DefaultDynamicAIClient implements DynamicAIClient {

    private final ChatModel chatModel;
    private final ImageModel imageModel;
    private final OpenAiChatOptions defaultChatOptions;
    private final OpenAiImageOptions defaultImageOptions;

    public DefaultDynamicAIClient(ChatModel chatModel,
                                  ImageModel imageModel,
                                  OpenAiChatOptions defaultChatOptions,
                                  OpenAiImageOptions defaultImageOptions) {
        this.chatModel = chatModel;
        this.imageModel = imageModel;
        this.defaultChatOptions = defaultChatOptions;
        this.defaultImageOptions = defaultImageOptions;
    }

    @Override
    public TextPromptBuilder prompt() {
        if (chatModel == null) {
            throw new AIException.ModelCallFailedException(
                "当前模型不支持文本生成，请使用 TEXT 类型的模型", null);
        }
        return new DefaultTextPromptBuilder();
    }

    @Override
    public ImagePromptBuilder imagePrompt() {
        if (imageModel == null) {
            throw new AIException.ModelCallFailedException(
                "当前模型不支持图片生成，请使用 IMAGE 类型的模型", null);
        }
        return new DefaultImagePromptBuilder();
    }

    /**
     * 默认文本提示构建器实现
     */
    private class DefaultTextPromptBuilder implements TextPromptBuilder {
        private String userMessage;
        private String systemMessage;
        private Double temperature;
        private Integer maxTokens;
        private final List<ToolCallback> toolCallbacks = new ArrayList<>();

        @Override
        public TextPromptBuilder user(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        @Override
        public TextPromptBuilder system(String systemMessage) {
            this.systemMessage = systemMessage;
            return this;
        }

        @Override
        public TextPromptBuilder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        @Override
        public TextPromptBuilder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        @Override
        public TextPromptBuilder tool(ToolCallback toolCallback) {
            if (toolCallback != null) {
                this.toolCallbacks.add(toolCallback);
            }
            return this;
        }

        @Override
        public TextPromptBuilder tools(List<ToolCallback> toolCallbacks) {
            if (toolCallbacks != null) {
                this.toolCallbacks.addAll(toolCallbacks);
            }
            return this;
        }

        @Override
        public ChatResponse call() {
            if (userMessage == null || userMessage.trim().isEmpty()) {
                throw new AIException.ModelCallFailedException("用户消息不能为空", null);
            }

            try {
                // 构建消息列表
                List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
                if (systemMessage != null && !systemMessage.trim().isEmpty()) {
                    messages.add(new SystemMessage(systemMessage));
                }
                messages.add(new UserMessage(userMessage));

                // 构建选项（运行时参数覆盖默认参数）
                OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder();
                if (defaultChatOptions != null) {
                    optionsBuilder.model(defaultChatOptions.getModel())
                            .temperature(defaultChatOptions.getTemperature())
                            .maxTokens(defaultChatOptions.getMaxTokens());
                }
                // 运行时参数覆盖
                if (temperature != null) {
                    optionsBuilder.temperature(temperature);
                }
                if (maxTokens != null) {
                    optionsBuilder.maxTokens(maxTokens);
                }
                // 添加工具回调
                if (!toolCallbacks.isEmpty()) {
                    optionsBuilder.toolCallbacks(toolCallbacks);
                }

                // 创建 Prompt 并调用
                Prompt prompt = new Prompt(messages, optionsBuilder.build());
                return chatModel.call(prompt);
            } catch (Exception e) {
                log.error("AI 文本生成失败: {}", e.getMessage(), e);
                throw new AIException.ModelCallFailedException("文本生成失败: " + e.getMessage(), e);
            }
        }

        @Override
        public String content() {
            ChatResponse response = call();
            if (response.getResults().isEmpty()) {
                throw new AIException.ModelCallFailedException("AI 返回空响应", null);
            }
            String result = response.getResults().get(0).getOutput().getText();
            int length = result != null ? result.length() : 0;
            log.debug("AI文本生成成功， 响应长度: {}",length);
            return result;
        }
    }

    /**
     * 默认图片提示构建器实现
     */
    private class DefaultImagePromptBuilder implements ImagePromptBuilder {
        private String promptText;
        private Integer width;
        private Integer height;

        @Override
        public ImagePromptBuilder prompt(String prompt) {
            this.promptText = prompt;
            return this;
        }

        @Override
        public ImagePromptBuilder width(Integer width) {
            this.width = width;
            return this;
        }

        @Override
        public ImagePromptBuilder height(Integer height) {
            this.height = height;
            return this;
        }

        @Override
        public ImageResponse call() {
            if (promptText == null || promptText.trim().isEmpty()) {
                throw new AIException.ModelCallFailedException("提示词不能为空", null);
            }

            try {
                // 构建选项（运行时参数覆盖默认参数）
                OpenAiImageOptions.Builder optionsBuilder = OpenAiImageOptions.builder();
                if (defaultImageOptions != null) {
                    optionsBuilder.model(defaultImageOptions.getModel())
                            .width(defaultImageOptions.getWidth())
                            .height(defaultImageOptions.getHeight());
                }
                // 运行时参数覆盖
                if (width != null) {
                    optionsBuilder.width(width);
                }
                if (height != null) {
                    optionsBuilder.height(height);
                }

                // 创建 ImagePrompt 并调用
                ImagePrompt imagePrompt = new ImagePrompt(promptText, optionsBuilder.build());
                return imageModel.call(imagePrompt);
            } catch (Exception e) {
                log.error("AI 图片生成失败: {}", e.getMessage(), e);
                throw new AIException.ModelCallFailedException("图片生成失败: " + e.getMessage(), e);
            }
        }

        @Override
        public String url() {
            ImageResponse response = call();
            if (response.getResults().isEmpty()) {
                throw new AIException.ModelCallFailedException("AI 返回空响应", null);
            }
            return response.getResult().getOutput().getUrl();
        }
    }
}
