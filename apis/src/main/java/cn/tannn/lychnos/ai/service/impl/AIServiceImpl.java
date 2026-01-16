package cn.tannn.lychnos.ai.service.impl;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.lychnos.ai.config.DynamicAIModelConfig;
import cn.tannn.lychnos.ai.exception.AIException;
import cn.tannn.lychnos.ai.factory.DynamicAIClientFactory;
import cn.tannn.lychnos.ai.service.AIService;
import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.dao.AIModelDao;
import cn.tannn.lychnos.entity.AIModel;
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
import java.util.List;
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

    private final AIModelDao aiModelDao;
    private final DynamicAIClientFactory clientFactory;

    /**
     * 默认图片风格提示词
     * 风格：现代信息图解风格，1024x576尺寸（16:9横向比例）
     */
    private static final String DEFAULT_IMAGE_STYLE_PROMPT = """
            Style Requirements:
            - Image size: 1024x576 pixels (16:9 aspect ratio, horizontal orientation)
            - Design style: Modern infographic poster with clean and organized layout
            - Background: Light neutral color (beige, light gray, or white) suitable for reading
            - Color scheme: Harmonious color palette with clear contrast for readability
            - Layout: Well-structured information sections with clear visual hierarchy
            - Typography: Clean, modern fonts with good readability; mix of bold headers and regular body text
            - Visual elements: Simple illustrations, icons, diagrams, flowcharts to support the content
            - Composition: Organized in logical sections with proper spacing and alignment
            - Decorative elements: Minimal decorative accents, focus on clarity and information delivery
            - Atmosphere: Professional, educational, informative, easy to understand
            - Quality: High resolution, suitable for web display
            - Information presentation: Use boxes, arrows, bullet points, and visual connectors to show relationships

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

            DynamicAIModelConfig config = buildConfig(aiModel);
            ImageModel imageModel = clientFactory.createImageModel(config, aiModel.getFactory());
            ImageResponse response = imageModel.call(new ImagePrompt(prompt));

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
        try {
            log.info("调用AI图片生成（流），modelId: {}, userId: {}, model: {}",
                    aiModel.getId(), aiModel.getUserId(), aiModel.getModel());

            // 先生成图片获取URL
            ImageResponse response = doGenerateImage(aiModel, prompt);

            // 从响应中提取图片URL
            String imageUrl = response.getResult().getOutput().getUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                throw new AIException.ModelCallFailedException("图片URL为空", null);
            }

            log.info("从URL下载图片流，url: {}", imageUrl);

            // 从URL下载图片并返回流
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();

            log.info("AI图片流生成成功，modelId: {}", aiModel.getId());
            return inputStream;
        } catch (Exception e) {
            log.error("AI图片流生成失败，modelId: {}, userId: {}, error: {}",
                    aiModel.getId(), aiModel.getUserId(), e.getMessage(), e);
            throw new AIException.ModelCallFailedException("图片流生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查找并验证模型权限和类型
     */
    private AIModel findAndVerifyModel(Long modelId, Long userId, ModelType expectedType) {
        AIModel aiModel = aiModelDao.findById(modelId)
                .orElseThrow(() -> new BusinessException("模型不存在，modelId: " + modelId));

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
     * 获取用户启用的模型
     */
    private AIModel getEnabledModel(Long userId, ModelType type) {
        List<AIModel> models = aiModelDao.findByUserIdAndTypeAndEnabled(userId, type, true);

        if (models.isEmpty()) {
            throw new AIException.ModelNotConfiguredException(
                    String.format("用户未配置可用的 %s 类型模型", type.name()));
        }

        // 返回第一个启用的模型（按创建时间倒序）
        return models.get(0);
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
     * 构建动态配置
     */
    private DynamicAIModelConfig buildConfig(AIModel aiModel) {
        return DynamicAIModelConfig.builder()
                .apiKey(aiModel.getApiKey())
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
