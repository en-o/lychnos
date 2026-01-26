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

    // ==================== 公共设置 ====================

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

    // ==================== HTTP 设置 ====================

    /**
     * HTTP 请求超时时间（秒）
     */
    @Builder.Default
    private Integer timeout = 60;

    /**
     * HTTP 连接超时时间（秒）
     */
    @Builder.Default
    private Integer connectTimeout = 30;

    /**
     * HTTP 读取超时时间（秒）
     */
    @Builder.Default
    private Integer readTimeout = 60;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private Integer maxRetries = 3;

    // ==================== Text 生成设置 ====================

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
     * Top P 采样参数（0-1之间）
     */
    @Builder.Default
    private Double topP = 1.0;

    /**
     * 频率惩罚（-2.0 到 2.0 之间）
     */
    @Builder.Default
    private Double frequencyPenalty = 0.0;

    /**
     * 存在惩罚（-2.0 到 2.0 之间）
     */
    @Builder.Default
    private Double presencePenalty = 0.0;

    // ==================== Image 生成设置 ====================

    /**
     * 图片生成默认尺寸（格式: "宽x高"，如 "1024x1024"）
     */
    @Builder.Default
    private String defaultImageSize = "1024x1024";

    /**
     * 图片生成推理步数（Z-Image-Turbo 推荐值为 9）
     */
    @Builder.Default
    private Integer imageInferenceSteps = 9;

    /**
     * 图片生成引导系数（Turbo 模型必须为 0.0）
     */
    @Builder.Default
    private Double imageGuidanceScale = 0.0;

    /**
     * 图片生成随机种子（-1 表示随机，固定值可保证可复现性）
     */
    @Builder.Default
    private Integer imageSeed = 42;

    /**
     * 图片生成数量
     */
    @Builder.Default
    private Integer imageCount = 1;

    /**
     * 图片质量（如: "standard", "hd"）
     */
    @Builder.Default
    private String imageQuality = "standard";

    /**
     * 图片风格（如: "vivid", "natural"）
     */
    @Builder.Default
    private String imageStyle = "vivid";

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
