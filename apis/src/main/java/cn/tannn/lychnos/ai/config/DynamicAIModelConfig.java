package cn.tannn.lychnos.ai.config;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.lychnos.common.constant.BusinessErrorCode;
import cn.tannn.lychnos.common.util.AESUtil;
import cn.tannn.lychnos.entity.AIModel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态 AI 模型配置
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
@Getter
@Builder
@Slf4j
public class DynamicAIModelConfig {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API Base URL
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 请求超时时间（秒）
     */
    @Builder.Default
    private Integer timeout = 60;

    /**
     * 温度参数（0-2之间，控制随机性）
     */
    @Builder.Default
    private Double temperature = 0.7;

    /**
     * 最大 tokens
     */
    @Builder.Default
    private Integer maxTokens = 2000;



    /**
     * 构建 ai 模型配置
     */
    public static DynamicAIModelConfig buildAiClientConfig(AIModel aiModel) {
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
}
