package cn.tannn.lychnos.controller.dto;

import cn.tannn.jdevelops.result.bean.SerializableBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户登录（账密）
 * @author tan
 * @date 2022/7/1  11:13
 */
@Schema(description = "用户登录（账密）")
@ToString
@Getter
@Setter
public class LoginPassword extends SerializableBean<LoginPassword> {
    /**
     * 登录名
     */
    @Schema(description = "登录名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String loginName;

    /**
     * 登录密码
     */
    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String password;


    public void setLoginName(String loginName) {
        this.loginName = StringUtils.trim(loginName);
    }

}
