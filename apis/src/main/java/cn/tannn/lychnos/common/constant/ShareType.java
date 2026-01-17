package cn.tannn.lychnos.common.constant;

import lombok.Getter;

/**
 * 模型分享状态
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/18
 */
@Getter
public enum ShareType {
    /**
     * 官方模型 - 系统官方提供，所有用户可用
     * <p>跟公开的区分在于这是内置的默认，兜底用的</p>
     */
    OFFICIAL(0, "官方"),

    /**
     * 私人模型 - 用户私有，仅创建者可用
     */
    PRIVATE(1, "私人"),

    /**
     * 公开模型 - 用户创建但公开分享，所有用户可见（预留）
     * <p>目前系统不支持公开模型的浏览和使用，此类型预留未来功能</p>
     */
    PUBLIC(2, "公开");

    /**
     * 状态码（对应数据库存储值）
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    ShareType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return ShareType 枚举，未找到返回 null
     */
    public static ShareType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ShareType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
