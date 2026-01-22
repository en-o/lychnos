package cn.tannn.lychnos.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth配置详情VO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/22
 */
@Schema(description = "OAuth配置详情VO")
@Getter
@Setter
@ToString
public class OAuthConfigDetailVO {

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "平台类型")
    private String providerType;

    @Schema(description = "平台名称")
    private String providerName;

    @Schema(description = "客户端ID")
    private String clientId;

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

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
