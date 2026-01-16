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
            ImageModel imageModel = clientFactory.createImageModel(config);
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
