package cn.tannn.lychnos.common.constant;

import lombok.Getter;

/**
 * 用途类型
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
@Getter
public enum UsageType {
    /**
     * 书籍提取
     */
    BOOK_EXTRACT("书籍提取"),

    /**
     * 书籍解析
     */
    BOOK_PARSE("书籍解析"),

    /**
     * 书籍生图
     */
    BOOK_IMAGE("书籍生图");

    private final String description;

    UsageType(String description) {
        this.description = description;
    }
}
