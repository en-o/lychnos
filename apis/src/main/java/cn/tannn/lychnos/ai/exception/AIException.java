package cn.tannn.lychnos.ai.exception;

/**
 * AI 调用异常
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
public class AIException extends RuntimeException {

    public AIException(String message) {
        super(message);
    }

    public AIException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 模型未配置异常
     */
    public static class ModelNotConfiguredException extends AIException {
        public ModelNotConfiguredException(String modelType) {
            super("未找到可用的" + modelType + "模型，请先配置");
        }
    }

    /**
     * 模型未启用异常
     */
    public static class ModelNotEnabledException extends AIException {
        public ModelNotEnabledException(String modelType) {
            super("未找到启用的" + modelType + "模型，请先启用");
        }
    }

    /**
     * 模型调用失败异常
     */
    public static class ModelCallFailedException extends AIException {
        public ModelCallFailedException(String message, Throwable cause) {
            super("AI模型调用失败: " + message, cause);
        }
    }

    /**
     * 响应解析失败异常
     */
    public static class ResponseParseException extends AIException {
        public ResponseParseException(String message, Throwable cause) {
            super("AI响应解析失败: " + message, cause);
        }
    }
}
