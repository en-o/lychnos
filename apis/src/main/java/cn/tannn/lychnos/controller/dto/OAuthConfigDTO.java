package cn.tannn.lychnos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth配置更新DTO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/22
 */
@Schema(description = "OAuth配置更新DTO")
@Getter
@Setter
@ToString
public class OAuthConfigDTO {

    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "客户端密钥")
    private String clientSecret;

    @Schema(description = "授权端点")
    private String authorizeUrl;

    @Schema(description = "获取Token端点")
    private String tokenUrl;

    @Schema(description = "获取用户信息端点")
    private String userInfoUrl;

    @Schema(description = "权限范围")
    private String scope;

    @Schema(description = "平台图标URL")
    private String iconUrl;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "Web回调地址前缀")
    private String webCallbackUrl;
}
