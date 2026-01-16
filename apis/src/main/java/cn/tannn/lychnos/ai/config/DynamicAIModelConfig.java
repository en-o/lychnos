package cn.tannn.lychnos.ai.config;

import lombok.Builder;
import lombok.Getter;

/**
 * 动态 AI 模型配置
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
@Getter
@Builder
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
}
