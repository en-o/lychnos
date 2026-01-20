package cn.tannn.lychnos.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 AI 重试配置
 * <p>
 * 限制重试次数，并只对可恢复的异常进行重试
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
@Slf4j
public class CustomRetryConfig {

    /**
     * 最大重试次数（不包括首次调用）
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * 创建自定义重试模板
     * <p>
     * 重试策略：
     * - 最多重试 2 次（总共 3 次调用）
     * - 只对可恢复的异常进行重试：
     *   - 429 Too Many Requests（速率限制）
     *   - 500/502/503/504 服务器错误
     *   - SocketTimeoutException（超时）
     * - 不重试的异常：
     *   - SSL 握手失败（SSLHandshakeException）
     *   - 其他 SSL 异常（SSLException）
     *   - 4xx 客户端错误（除了 429）
     *   - NonTransientAiException（Spring AI 标记的不可重试异常）
     *
     * @return RetryTemplate
     */
    public static RetryTemplate createRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 配置重试策略
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();

        // 可重试的异常
        retryableExceptions.put(HttpServerErrorException.class, true); // 5xx 错误
        retryableExceptions.put(SocketTimeoutException.class, true);   // 超时

        // 不可重试的异常
        retryableExceptions.put(SSLHandshakeException.class, false);   // SSL 握手失败
        retryableExceptions.put(SSLException.class, false);            // 其他 SSL 异常
        retryableExceptions.put(NonTransientAiException.class, false); // Spring AI 不可重试异常
        retryableExceptions.put(HttpClientErrorException.Unauthorized.class, false); // 401 认证失败
        retryableExceptions.put(HttpClientErrorException.Forbidden.class, false);    // 403 禁止访问
        retryableExceptions.put(HttpClientErrorException.BadRequest.class, false);   // 400 错误请求

        // 创建异常分类器
        BinaryExceptionClassifier exceptionClassifier = new BinaryExceptionClassifier(retryableExceptions, false);

        // 特殊处理：429 Too Many Requests 应该重试
        RetryPolicy retryPolicy = new SimpleRetryPolicy(MAX_RETRY_ATTEMPTS + 1) {
            @Override
            public boolean canRetry(org.springframework.retry.RetryContext context) {
                Throwable lastThrowable = context.getLastThrowable();

                if (lastThrowable == null) {
                    return super.canRetry(context);
                }

                // 检查是否是 429 错误（速率限制）
                if (lastThrowable instanceof HttpClientErrorException.TooManyRequests) {
                    log.warn("遇到速率限制（429），将进行重试，当前重试次数: {}", context.getRetryCount());
                    return context.getRetryCount() < MAX_RETRY_ATTEMPTS + 1;
                }

                // 检查是否是 ResourceAccessException（可能包含 SSL 异常）
                if (lastThrowable instanceof ResourceAccessException) {
                    Throwable cause = lastThrowable.getCause();
                    if (cause instanceof SSLException) {
                        log.warn("遇到 SSL 异常，不进行重试: {}", cause.getMessage());
                        return false;
                    }
                }

                // 使用异常分类器判断
                boolean shouldRetry = exceptionClassifier.classify(lastThrowable);

                if (!shouldRetry) {
                    log.warn("遇到不可重试的异常，停止重试: {}", lastThrowable.getClass().getSimpleName());
                } else if (context.getRetryCount() > 0) {
                    log.warn("重试 AI 调用，当前重试次数: {}, 异常: {}",
                            context.getRetryCount(), lastThrowable.getMessage());
                }

                return shouldRetry && super.canRetry(context);
            }
        };

        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
