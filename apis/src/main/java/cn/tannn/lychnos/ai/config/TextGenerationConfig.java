package cn.tannn.lychnos.ai.config;

import lombok.Builder;
import lombok.Getter;

/**
 * Text 生成配置
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/26
 */
@Getter
@Builder
public class TextGenerationConfig {

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
}
