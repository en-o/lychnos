package cn.tannn.lychnos.common.constant;

import lombok.Getter;

/**
 * 业务错误码枚举
 * 统一管理所有业务异常的错误码和错误消息
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/17
 */
@Getter
public enum BusinessErrorCode {

    /**
     * 1001: 书籍已分析过
     */
    BOOK_ALREADY_ANALYZED(1001, "该书籍已经分析过，请到历史记录中查看"),

    /**
     * 1002: 用户未配置可用的 AI 模型
     */
    MODEL_NOT_CONFIGURED(1002, "用户未配置可用的 %s 类型模型，请先配置");

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息模板（支持占位符 %s）
     */
    private final String message;

    BusinessErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取格式化后的错误消息
     *
     * @param args 消息参数
     * @return 格式化后的错误消息
     */
    public String formatMessage(Object... args) {
        return String.format(message, args);
    }
}
