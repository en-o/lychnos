package cn.tannn.lychnos.ai.config;

import lombok.Builder;
import lombok.Getter;

/**
 * HTTP 请求配置
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/26
 */
@Getter
@Builder
public class HttpConfig {

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
}
