package cn.tannn.lychnos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 绑定第三方账户请求 DTO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Data
@Schema(description = "绑定第三方账户请求")
public class BindAccountDTO {

    @Schema(description = "平台类型", example = "GITHUB")
    @NotBlank(message = "平台类型不能为空")
    private String providerType;

    @Schema(description = "授权码")
    @NotBlank(message = "授权码不能为空")
    private String code;

    @Schema(description = "状态码（防CSRF）")
    private String state;
}
