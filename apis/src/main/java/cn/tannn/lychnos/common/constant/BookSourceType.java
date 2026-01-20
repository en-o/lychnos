package cn.tannn.lychnos.common.constant;

import lombok.Getter;

/**
 * 书籍来源类型枚举
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
@Getter
public enum BookSourceType {
    /**
     * 用户输入的书籍（AI识别出的）
     */
    USER_INPUT("USER_INPUT", "您输入的书籍"),

    /**
     * 相似推荐书籍
     */
    SIMILAR("SIMILAR", "相似推荐"),

    /**
     * 未找到时的推荐书籍
     */
    NOT_FOUND_RECOMMEND("NOT_FOUND_RECOMMEND", "您可能在找这本书"),

    /**
     * 数据库中已分析过的书籍
     */
    ALREADY_ANALYZED("ALREADY_ANALYZED", "该书籍已分析过");

    private final String code;
    private final String defaultLabel;

    BookSourceType(String code, String defaultLabel) {
        this.code = code;
        this.defaultLabel = defaultLabel;
    }

    /**
     * 根据code获取枚举
     */
    public static BookSourceType fromCode(String code) {
        if (code == null) {
            return USER_INPUT;
        }
        for (BookSourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return USER_INPUT;
    }
}
