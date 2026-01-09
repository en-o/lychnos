package cn.tannn.lychnos.controller.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author tan
 */
@Schema(description = "修改密码")
@ToString
@Getter
@Setter
public class PasswordEdit {

    /**
     * 旧登录密码
     */
    @Schema(description = "旧登录密码",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String oldPassword;

    /**
     * 新登录密码
     */
    @Schema(description = "新登录密码",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String newPassword;

}
