package cn.tannn.lychnos.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * OAuth2 Provider VO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Data
@Schema(description = "第三方登录平台信息")
public class OAuth2ProviderVO {

    @Schema(description = "平台类型标识")
    private String type;

    @Schema(description = "平台显示名称")
    private String name;

    @Schema(description = "平台图标URL")
    private String iconUrl;

    @Schema(description = "排序顺序")
    private Integer sortOrder;
}
