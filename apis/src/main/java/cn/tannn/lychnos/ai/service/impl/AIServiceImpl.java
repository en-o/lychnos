package cn.tannn.lychnos.ai.service.impl;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.lychnos.ai.config.DynamicAIModelConfig;
import cn.tannn.lychnos.ai.exception.AIException;
import cn.tannn.lychnos.ai.factory.DynamicAIClientFactory;
import cn.tannn.lychnos.ai.service.AIService;
import cn.tannn.lychnos.common.constant.ModelType;
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

import java.util.List;

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

    private final AIModelService aiModelService;
    private final DynamicAIClientFactory clientFactory;

    @Override
    public String generateText(Long userId, String prompt) {
        // 查询用户启用的文本模型
        AIModel aiModel = getEnabledModel(userId, ModelType.TEXT);

        return generateTextWithModel(aiModel.getId(), userId, prompt);
    }

    @Override
    public String generateTextWithModel(Long modelId, Long userId, String prompt) {
        // 查询并验证模型权限
        AIModel aiModel = aiModelService.findVerifyRole(modelId, userId);

        if (aiModel.getType() != ModelType.TEXT) {
            throw new BusinessException("模型类型不匹配，期望TEXT类型");
        }

        try {
            log.info("调用AI文本生成，modelId: {}, userId: {}, model: {}",
                    modelId, userId, aiModel.getModel());

            // 构建动态配置
            DynamicAIModelConfig config = buildConfig(aiModel);

            // 创建聊天模型
            ChatModel chatModel = clientFactory.createChatModel(config);

            // 调用 AI
            ChatResponse response = chatModel.call(new Prompt(prompt));

            // 返回结果
            if (!response.getResults().isEmpty()) {
                String result = response.getResults().get(0).getOutput().getText();
                int length = 0;
                if(result != null){
                    length  = result.length();
                }
                log.info("AI文本生成成功，modelId: {}, 响应长度: {}", modelId, length);
                return result;
            } else {
                throw new AIException.ModelCallFailedException("AI返回空响应", null);
            }
        } catch (Exception e) {
            log.error("AI文本生成失败，modelId: {}, userId: {}, prompt: {}",
                    modelId, userId, prompt, e);
            throw new AIException.ModelCallFailedException(e.getMessage(), e);
        }
    }

    @Override
    public ImageResponse generateImage(Long userId, String prompt) {
        // 查询用户启用的图片模型
        AIModel aiModel = getEnabledModel(userId, ModelType.IMAGE);

        return generateImageWithModel(aiModel.getId(), userId, prompt);
    }

    @Override
    public ImageResponse generateImageWithModel(Long modelId, Long userId, String prompt) {
        // 查询并验证模型权限
        AIModel aiModel = aiModelService.findVerifyRole(modelId, userId);

        if (aiModel.getType() != ModelType.IMAGE) {
            throw new BusinessException("模型类型不匹配，期望IMAGE类型");
        }

        try {
            log.info("调用AI图片生成，modelId: {}, userId: {}, model: {}",
                    modelId, userId, aiModel.getModel());

            // 构建动态配置
            DynamicAIModelConfig config = buildConfig(aiModel);

            // 创建图片模型
            ImageModel imageModel = clientFactory.createImageModel(config);

            // 调用 AI
            ImageResponse response = imageModel.call(new ImagePrompt(prompt));
            log.info("AI图片生成成功，modelId: {}", modelId);
            return response;
        } catch (Exception e) {
            log.error("AI图片生成失败，modelId: {}, userId: {}, prompt: {}",
                    modelId, userId, prompt, e);
            throw new AIException.ModelCallFailedException(e.getMessage(), e);
        }
    }

    /**
     * 获取用户启用的模型
     */
    private AIModel getEnabledModel(Long userId, ModelType type) {
        List<AIModel> models = aiModelService.findByUserIdAndType(userId, type);

        if (models.isEmpty()) {
            throw new AIException.ModelNotConfiguredException(type.name());
        }

        // 查找第一个启用的模型
        return models.stream()
                .filter(model -> Boolean.TRUE.equals(model.getEnabled()))
                .findFirst()
                .orElseThrow(() -> new AIException.ModelNotEnabledException(type.name()));
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
}
