package cn.tannn.lychnos.ai.service.impl;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.lychnos.ai.config.DynamicAIModelConfig;
import cn.tannn.lychnos.ai.exception.AIException;
import cn.tannn.lychnos.ai.factory.DynamicAIClientFactory;
import cn.tannn.lychnos.ai.service.AIService;
import cn.tannn.lychnos.common.constant.BusinessErrorCode;
import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.common.util.AESUtil;
import cn.tannn.lychnos.common.util.ZipUtil;
import cn.tannn.lychnos.entity.AIModel;
import cn.tannn.lychnos.service.AIModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * AI 服务实现
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final DynamicAIClientFactory clientFactory;
    private final AIModelService aiModelService;

    /**
     * 默认图片风格提示词
     * 风格：现代信息图解风格，1024x576尺寸（16:9横向比例）
     * 强制横向布局，内容密集
     */
    private static final String DEFAULT_IMAGE_STYLE_PROMPT = """
            CRITICAL REQUIREMENTS (MUST FOLLOW):
            - Image size: EXACTLY 1024x576 pixels (16:9 aspect ratio)
            - Orientation: HORIZONTAL LANDSCAPE (width MUST be 1024, height MUST be 576)
            - DO NOT create vertical/portrait images

            Style Requirements:
            - Design style: Modern infographic poster with dense, information-rich layout
            - Background: Light neutral color (beige, light gray, or white) suitable for reading
            - Color scheme: Harmonious color palette with clear contrast for readability
            - Layout: Multi-section horizontal layout with maximum information density
            - Typography: Clean, modern fonts; mix of bold headers and regular body text
            - Visual elements: Multiple illustrations, icons, diagrams, charts to fill the space
            - Composition: Divide horizontally into 2-3 main columns or sections
            - Information density: HIGH - utilize all available horizontal space
            - Decorative elements: Minimal, focus on information delivery
            - Atmosphere: Professional, educational, informative
            - Quality: High resolution, suitable for web display
            - Information presentation: Use boxes, arrows, bullet points, numbered lists, and visual connectors

            Layout Suggestions:
            - Left section: Main title and key information
            - Middle section: Core content with icons/diagrams
            - Right section: Additional details or summary
            - Use horizontal dividers and borders to organize content

            """;

    @Override
    public String generateText(Long userId, String prompt) {
        validateUserId(userId);
        validatePrompt(prompt);
        // 获取用户启用的默认模型
        AIModel aiModel = getEnabledModel(userId, ModelType.TEXT);
        // 直接使用已查询的模型进行生成
        return doGenerateText(aiModel, prompt);
    }

    @Override
    public String generateTextWithModel(Long modelId, Long userId, String prompt) {
        validateModelId(modelId);
        validateUserId(userId);
        validatePrompt(prompt);

        // 查询并验证模型
        AIModel aiModel = findAndVerifyModel(modelId, userId, ModelType.TEXT);

        // 使用指定模型进行生成
        return doGenerateText(aiModel, prompt);
    }

    @Override
    public ImageResponse generateImage(Long userId, String prompt) {
        validateUserId(userId);
        validatePrompt(prompt);

        // 获取用户启用的默认模型
        AIModel aiModel = getEnabledModel(userId, ModelType.IMAGE);

        // 直接使用已查询的模型进行生成
        return doGenerateImage(aiModel, prompt);
    }

    @Override
    public ImageResponse generateImageWithModel(Long modelId, Long userId, String prompt) {
        validateModelId(modelId);
        validateUserId(userId);
        validatePrompt(prompt);

        // 查询并验证模型
        AIModel aiModel = findAndVerifyModel(modelId, userId, ModelType.IMAGE);

        // 使用指定模型进行生成
        return doGenerateImage(aiModel, prompt);
    }

    @Override
    public ImageResponse generateImageWithContent(Long userId, String contentPrompt) {
        validateUserId(userId);
        validatePrompt(contentPrompt);

        // 获取用户启用的默认模型
        AIModel aiModel = getEnabledModel(userId, ModelType.IMAGE);

        // 拼接默认风格提示词和内容提示词
        String fullPrompt = buildFullImagePrompt(contentPrompt);

        // 生成图片
        return doGenerateImage(aiModel, fullPrompt);
    }

    @Override
    public ImageResponse generateImageWithContentAndModel(Long modelId, Long userId, String contentPrompt) {
        validateModelId(modelId);
        validateUserId(userId);
        validatePrompt(contentPrompt);

        // 查询并验证模型
        AIModel aiModel = findAndVerifyModel(modelId, userId, ModelType.IMAGE);

        // 拼接默认风格提示词和内容提示词
        String fullPrompt = buildFullImagePrompt(contentPrompt);

        // 使用指定模型进行生成
        return doGenerateImage(aiModel, fullPrompt);
    }

    @Override
    public InputStream generateImageStream(Long userId, String prompt) {
        validateUserId(userId);
        validatePrompt(prompt);

        // 获取用户启用的默认模型
        AIModel aiModel = getEnabledModel(userId, ModelType.IMAGE);

        // 生成图片并返回流
        return doGenerateImageStream(aiModel, prompt);
    }

    @Override
    public InputStream generateImageStreamWithModel(Long modelId, Long userId, String prompt) {
        validateModelId(modelId);
        validateUserId(userId);
        validatePrompt(prompt);

        // 查询并验证模型
        AIModel aiModel = findAndVerifyModel(modelId, userId, ModelType.IMAGE);

        // 使用指定模型生成图片并返回流
        return doGenerateImageStream(aiModel, prompt);
    }

    @Override
    public InputStream generateImageStreamWithContent(Long userId, String contentPrompt) {
        validateUserId(userId);
        validatePrompt(contentPrompt);

        // 获取用户启用的默认模型
        AIModel aiModel = getEnabledModel(userId, ModelType.IMAGE);

        // 拼接默认风格提示词和内容提示词
        String fullPrompt = buildFullImagePrompt(contentPrompt);

        // 生成图片并返回流
        return doGenerateImageStream(aiModel, fullPrompt);
    }

    @Override
    public InputStream generateImageStreamWithContentAndModel(Long modelId, Long userId, String contentPrompt) {
        validateModelId(modelId);
        validateUserId(userId);
        validatePrompt(contentPrompt);

        // 查询并验证模型
        AIModel aiModel = findAndVerifyModel(modelId, userId, ModelType.IMAGE);

        // 拼接默认风格提示词和内容提示词
        String fullPrompt = buildFullImagePrompt(contentPrompt);

        // 使用指定模型生成图片并返回流
        return doGenerateImageStream(aiModel, fullPrompt);
    }

    /**
     * 执行文本生成（内部方法，避免重复查询）
     */
    private String doGenerateText(AIModel aiModel, String prompt) {
        try {
            log.info("调用AI文本生成，modelId: {}, userId: {}, model: {}",
                    aiModel.getId(), aiModel.getUserId(), aiModel.getModel());

            DynamicAIModelConfig config = buildConfig(aiModel);
            ChatModel chatModel = clientFactory.createChatModel(config);
            ChatResponse response = chatModel.call(new Prompt(prompt));

            return extractTextFromResponse(response, aiModel.getId());
        } catch (Exception e) {
            log.error("AI文本生成失败，modelId: {}, userId: {}, error: {}",
                    aiModel.getId(), aiModel.getUserId(), e.getMessage(), e);
            throw new AIException.ModelCallFailedException("文本生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行图片生成（内部方法，避免重复查询）
     */
    private ImageResponse doGenerateImage(AIModel aiModel, String prompt) {
        try {
            log.info("调用AI图片生成，modelId: {}, userId: {}, model: {}",
                    aiModel.getId(), aiModel.getUserId(), aiModel.getModel());

            // 压缩提示词：去除多余空格、换行、制表符（减少token消耗）
            String compressedPrompt = ZipUtil.smartCompressPrompt(prompt, 1999);
            if (compressedPrompt.length() != prompt.length()) {
                log.info("提示词已压缩，原始: {} -> 压缩: {} (节省 {} 字符)",
                        prompt.length(), compressedPrompt.length(),
                        prompt.length() - compressedPrompt.length());
            }

            DynamicAIModelConfig config = buildConfig(aiModel);
            ImageModel imageModel = clientFactory.createImageModel(config, aiModel.getFactory());
            ImageResponse response = imageModel.call(new ImagePrompt(compressedPrompt));

            log.info("AI图片生成成功，modelId: {}", aiModel.getId());
            return response;
        } catch (Exception e) {
            log.error("AI图片生成失败，modelId: {}, userId: {}, error: {}",
                    aiModel.getId(), aiModel.getUserId(), e.getMessage(), e);
            throw new AIException.ModelCallFailedException("图片生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行图片生成并返回流（内部方法，避免重复查询）
     */
    private InputStream doGenerateImageStream(AIModel aiModel, String prompt) {
        int maxRetries = 3;
        int retryDelay = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("调用AI图片生成（流），modelId: {}, userId: {}, model: {}, 尝试: {}/{}",
                        aiModel.getId(), aiModel.getUserId(), aiModel.getModel(), attempt, maxRetries);

                ImageResponse response = doGenerateImage(aiModel, prompt);
                String imageUrl = response.getResult().getOutput().getUrl();
                if (imageUrl == null || imageUrl.isEmpty()) {
                    throw new AIException.ModelCallFailedException("图片URL为空", null);
                }

                log.info("从URL下载图片流，url: {}", imageUrl);
                URL url = new URL(imageUrl);
                InputStream inputStream = url.openStream();

                log.info("AI图片流生成成功，modelId: {}", aiModel.getId());
                return inputStream;
            } catch (javax.net.ssl.SSLHandshakeException e) {
                log.warn("SSL握手失败（尝试 {}/{}），modelId: {}, error: {}",
                        attempt, maxRetries, aiModel.getId(), e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw new AIException.ModelCallFailedException("图片流生成失败（SSL握手失败，已重试" + maxRetries + "次）: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                log.error("AI图片流生成失败，modelId: {}, userId: {}, error: {}",
                        aiModel.getId(), aiModel.getUserId(), e.getMessage(), e);
                throw new AIException.ModelCallFailedException("图片流生成失败: " + e.getMessage(), e);
            }
        }
        throw new AIException.ModelCallFailedException("图片流生成失败（已重试" + maxRetries + "次）", null);
    }

    /**
     * 查找并验证模型权限和类型
     */
    private AIModel findAndVerifyModel(Long modelId, Long userId, ModelType expectedType) {
        AIModel aiModel = aiModelService.findById(modelId);

        // 验证用户权限
        if (!Objects.equals(aiModel.getUserId(), userId)) {
            throw new BusinessException("无权访问该模型");
        }

        // 验证模型类型
        if (aiModel.getType() != expectedType) {
            throw new BusinessException(
                    String.format("模型类型不匹配，期望 %s 类型，实际 %s 类型",
                            expectedType, aiModel.getType()));
        }

        // 验证模型是否启用 - id方式不验证启用·
//        if (!Boolean.TRUE.equals(aiModel.getEnabled())) {
//            throw new AIException.ModelNotEnabledException(
//                    String.format("模型已禁用，modelId: %s", modelId));
//        }

        return aiModel;
    }

    /**
     * 获取用户启用的模型（支持官方模型回退）
     * <p>查询逻辑：</p>
     * <ol>
     *     <li>优先使用用户自己启用的模型</li>
     *     <li>如果用户未配置，则回退到官方模型（share=0）</li>
     *     <li>如果官方模型也不存在，则抛出异常</li>
     * </ol>
     * <p>注意：此方法调用 AIModelService.getEnabledModel() 获取模型，然后进行 API Key 验证</p>
     */
    private AIModel getEnabledModel(Long userId, ModelType type) {
        // 调用 AIModelService 获取模型（包含官方模型回退逻辑）
        AIModel model = aiModelService.getEnabledModel(userId, type);

        // 如果没有找到任何可用模型，抛出异常
        if (model == null) {
            log.warn("用户未配置模型且无可用的官方模型，userId: {}, type: {}", userId, type);
            throw new BusinessException(
                    BusinessErrorCode.MODEL_NOT_CONFIGURED.getCode(),
                    BusinessErrorCode.MODEL_NOT_CONFIGURED.formatMessage(type.name())
            );
        }

        // 验证 API Key 是否有效
        validateApiKey(model, userId, type);

        return model;
    }

    /**
     * 验证模型的 API Key 是否有效
     *
     * @param model 模型对象
     * @param userId 用户ID（用于日志）
     * @param type 模型类型（用于错误提示）
     */
    private void validateApiKey(AIModel model, Long userId, ModelType type) {
        //  验证 API Key 是否有效（在解密之前检查）
        if (model.getApiKey() == null || model.getApiKey().trim().isEmpty()) {
            log.warn("模型 API Key 为空，modelId: {}, userId: {}, type: {}",
                    model.getId(), userId, type);
            throw new BusinessException(
                    BusinessErrorCode.MODEL_NOT_CONFIGURED.getCode(),
                    BusinessErrorCode.MODEL_NOT_CONFIGURED.formatMessage(type.name())
            );
        }
    }

    /**
     * 从响应中提取文本
     */
    private String extractTextFromResponse(ChatResponse response, Long modelId) {
        if (response == null || response.getResults().isEmpty()) {
            throw new AIException.ModelCallFailedException("AI返回空响应", null);
        }

        String result = response.getResults().get(0).getOutput().getText();
        int length = result != null ? result.length() : 0;

        log.info("AI文本生成成功，modelId: {}, 响应长度: {}", modelId, length);
        return result;
    }

    /**
     * 构建动态配置（解密 API Key 用于实际调用）
     */
    private DynamicAIModelConfig buildConfig(AIModel aiModel) {
        // 解密 API Key
        String decryptedApiKey = null;
        if (aiModel.getApiKey() != null && !aiModel.getApiKey().isEmpty()) {
            try {
                decryptedApiKey = AESUtil.decrypt(aiModel.getApiKey());
            } catch (Exception e) {
                log.error("解密 API Key 失败，模型ID: {}, type: {}, error: {}",
                        aiModel.getId(), aiModel.getType(), e.getMessage(), e);
                // API Key 格式错误也视为配置无效
                throw new BusinessException(
                        BusinessErrorCode.MODEL_NOT_CONFIGURED.getCode(),
                        String.format("API Key 格式错误，请重新配置 %s 类型模型", aiModel.getType().name())
                );
            }
        }

        return DynamicAIModelConfig.builder()
                .apiKey(decryptedApiKey)
                .baseUrl(aiModel.getApiUrl())
                .model(aiModel.getModel())
                .build();
    }

    /**
     * 构建完整的图片提示词（默认风格 + 内容描述）
     *
     * @param contentPrompt 内容提示词
     * @return 完整提示词
     */
    private String buildFullImagePrompt(String contentPrompt) {
        return DEFAULT_IMAGE_STYLE_PROMPT + "\nContent Description:\n" + contentPrompt;
    }


    /**
     * 参数验证方法
     */
    private void validateModelId(Long modelId) {
        if (modelId == null || modelId <= 0) {
            throw new BusinessException("模型ID不能为空或无效");
        }
    }

    /**
     * 验证账户参数
     * @param userId userId
     */
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空或无效");
        }
    }

    /**
     * 验证提示词参数
     * @param prompt prompt
     */
    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new BusinessException("提示词不能为空");
        }
    }
}
