package cn.tannn.lychnos.entity;

import cn.tannn.lychnos.common.pojo.JpaCommonBean;
import cn.tannn.lychnos.common.util.AESUtil;
import cn.tannn.lychnos.enums.ProviderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * OAuth2 第三方平台配置表
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Entity
@Table(name = "tb_oauth_config", indexes = {
        @Index(name = "idx_provider_type", columnList = "providerType")
})
@Comment("OAuth2第三方平台配置表")
@Getter
@Setter
@ToString(exclude = { "clientSecret" }) // 排除敏感信息
@DynamicUpdate
@DynamicInsert
@Schema(description = "OAuth2第三方平台配置表")
@Slf4j
public class OAuthConfig extends JpaCommonBean<OAuthConfig> {

    /**
     * 第三方平台类型
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50) not null")
    @Comment("第三方平台类型")
    @Schema(description = "第三方平台类型")
    private ProviderType providerType;

    /**
     * 客户端ID（数据库存储加密后的值）
     */
    @Column(columnDefinition = "varchar(500) not null")
    @Comment("客户端ID（加密存储）")
    @Schema(description = "客户端ID")
    private String clientId;

    /**
     * 客户端密钥（数据库存储加密后的值）
     */
    @Column(columnDefinition = "varchar(500) not null")
    @Comment("客户端密钥（加密存储）")
    @Schema(description = "客户端密钥")
    private String clientSecret;

    /**
     * 授权端点
     */
    @Column(columnDefinition = "varchar(500) not null")
    @Comment("授权端点")
    @Schema(description = "授权端点")
    private String authorizeUrl;

    /**
     * 获取Token端点
     */
    @Column(columnDefinition = "varchar(500) not null")
    @Comment("获取Token端点")
    @Schema(description = "获取Token端点")
    private String tokenUrl;

    /**
     * 获取用户信息端点
     */
    @Column(columnDefinition = "varchar(500) not null")
    @Comment("获取用户信息端点")
    @Schema(description = "获取用户信息端点")
    private String userInfoUrl;

    /**
     * 权限范围
     */
    @Column(columnDefinition = "varchar(200)")
    @Comment("权限范围")
    @Schema(description = "权限范围")
    private String scope;

    /**
     * 平台图标URL
     */
    @Column(columnDefinition = "varchar(500)")
    @Comment("平台图标URL")
    @Schema(description = "平台图标URL")
    private String iconUrl;

    /**
     * 排序顺序
     */
    @Column(columnDefinition = "int")
    @ColumnDefault("0")
    @Comment("排序顺序")
    @Schema(description = "排序顺序")
    private Integer sortOrder;

    // ============ 加密/解密辅助方法 ============

    /**
     * 持久化之前自动加密敏感字段
     * 注意：从数据库加载后，实体对象中的值已被@PostLoad解密为明文
     * 因此这里直接加密即可，不需要判断是否已加密
     */
    @PrePersist
    @PreUpdate
    private void encryptSensitiveFields() {
        try {
            if (this.clientId != null && !this.clientId.isEmpty()) {
                this.clientId = AESUtil.encrypt(this.clientId);
            }
            if (this.clientSecret != null && !this.clientSecret.isEmpty()) {
                this.clientSecret = AESUtil.encrypt(this.clientSecret);
            }
        } catch (Exception e) {
            log.error("加密 OAuth2 配置敏感字段失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 加载后自动解密敏感字段
     * 注意：从数据库加载的值是密文，需要解密为明文供业务层使用
     */
    @PostLoad
    private void decryptSensitiveFields() {
        try {
            if (this.clientId != null && !this.clientId.isEmpty()) {
                this.clientId = AESUtil.decrypt(this.clientId);
            }
            if (this.clientSecret != null && !this.clientSecret.isEmpty()) {
                this.clientSecret = AESUtil.decrypt(this.clientSecret);
            }
        } catch (Exception e) {
            log.error("解密 OAuth2 配置敏感字段失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }
}
