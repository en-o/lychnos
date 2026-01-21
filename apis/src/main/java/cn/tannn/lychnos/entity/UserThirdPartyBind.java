package cn.tannn.lychnos.entity;

import cn.tannn.lychnos.common.pojo.JpaCommonBean;
import cn.tannn.lychnos.enums.ProviderType;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 用户第三方账户绑定表
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Entity
@Table(name = "tb_user_third_party_bind", indexes = {
                @Index(name = "idx_user_id", columnList = "userId"),
                @Index(name = "idx_provider_type", columnList = "providerType")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_provider_openid", columnNames = { "providerType", "openId" }),
                @UniqueConstraint(name = "uk_user_provider", columnNames = { "userId", "providerType" })
})
@Comment("用户第三方账户绑定表")
@Getter
@Setter
@ToString
@DynamicUpdate
@DynamicInsert
@Schema(description = "用户第三方账户绑定表")
public class UserThirdPartyBind extends JpaCommonBean<UserThirdPartyBind> {

        /**
         * 用户ID
         */
        @Column(columnDefinition = "bigint not null")
        @Comment("用户ID")
        @Schema(description = "用户ID")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long userId;

        /**
         * 第三方平台类型
         */
        @Enumerated(EnumType.STRING)
        @Column(columnDefinition = "varchar(50) not null")
        @Comment("第三方平台类型")
        @Schema(description = "第三方平台类型")
        private ProviderType providerType;

        /**
         * 第三方平台用户唯一标识（如 GitHub 的 user.id）
         */
        @Column(columnDefinition = "varchar(200) not null")
        @Comment("第三方平台用户唯一标识")
        @Schema(description = "第三方平台用户唯一标识")
        private String openId;

        /**
         * 第三方平台 UnionID（如微信的 unionId，可选）
         */
        @Column(columnDefinition = "varchar(200)")
        @Comment("第三方平台UnionID")
        @Schema(description = "第三方平台UnionID（如微信）")
        private String unionId;

        /**
         * 第三方平台昵称
         */
        @Column(columnDefinition = "varchar(100)")
        @Comment("第三方平台昵称")
        @Schema(description = "第三方平台昵称")
        private String nickname;

        /**
         * 第三方平台头像URL
         */
        @Column(columnDefinition = "varchar(500)")
        @Comment("第三方平台头像URL")
        @Schema(description = "第三方平台头像URL")
        private String avatarUrl;

        /**
         * 第三方平台邮箱
         */
        @Column(columnDefinition = "varchar(100)")
        @Comment("第三方平台邮箱")
        @Schema(description = "第三方平台邮箱")
        private String email;

        /**
         * 其他额外信息（JSON对象）
         */
        @Column(columnDefinition = "json")
        @Comment("其他额外信息（JSON格式）")
        @Schema(description = "其他额外信息（JSON格式）")
        @JdbcTypeCode(SqlTypes.JSON)
        private JSONObject extraInfo;
}
