package cn.tannn.lychnos.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户第三方账户绑定 VO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Data
@Schema(description = "用户第三方账户绑定信息")
public class UserThirdPartyBindVO {

    @Schema(description = "平台类型")
    private String providerType;

    @Schema(description = "第三方平台昵称")
    private String nickname;

    @Schema(description = "第三方平台头像URL")
    private String avatarUrl;

    @Schema(description = "第三方平台邮箱")
    private String email;

    @Schema(description = "绑定时间")
    private LocalDateTime createTime;
}
