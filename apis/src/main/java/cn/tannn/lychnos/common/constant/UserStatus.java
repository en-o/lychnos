package cn.tannn.lychnos.common.constant;

import lombok.Getter;

/**
 * 用户状态
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/18
 */
@Getter
public enum UserStatus {
    /**
     * 正常
     */
    NORMAL(1, "正常"),
    /**
     * 注销 - 暂时没用
     */
    DELETE(2, "注销"),
    /**
     * 违规封禁
     */
    BANNED(3, "违规封禁");

    /**
     * 状态码（对应数据库存储值）
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return ShareType 枚举，未找到返回 null
     */
    public static UserStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
