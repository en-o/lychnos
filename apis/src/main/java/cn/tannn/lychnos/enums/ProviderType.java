package cn.tannn.lychnos.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2 第三方平台类型枚举
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Getter
@RequiredArgsConstructor
public enum ProviderType {

    /**
     * GitHub
     */
    GITHUB("GITHUB", "GitHub"),

    /**
     * LinuxDo 论坛
     */
    LINUXDO("LINUXDO", "LinuxDo"),

    /**
     * QQ
     */
    QQ("QQ", "QQ"),

    /**
     * 微信
     */
    WECHAT("WECHAT", "微信");

    /**
     * 枚举值（用于数据库存储）
     */
    @JsonValue
    private final String value;

    /**
     * 显示名称（用于前端展示）
     */
    private final String displayName;

    /**
     * 根据字符串值获取枚举
     *
     * @param value 字符串值
     * @return ProviderType枚举
     */
    public static ProviderType fromValue(String value) {
        for (ProviderType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的第三方平台类型: " + value);
    }
}
