package cn.tannn.lychnos.controller.dto;

import cn.tannn.jdevelops.result.bean.SerializableBean;
import cn.tannn.lychnos.entity.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 注册用户
 *
 * @author <a href="https://tannn.cn/">tan</a>
 * @date 2023/11/21 9:56
 */
@Schema(description = "注册用户")
@ToString
@Getter
@Setter
public class UserInfoRegister extends SerializableBean<UserInfoRegister> {
    // 基本信息
    @Schema(description = "登录名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录名不能为空")
    @Size(min = 3, max = 50, message = "登录名长度必须在4-50个字符之间")
    private String loginName;

    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 3, max = 30, message = "密码长度必须在3-30个字符之间")
//    @Password
    private String password;

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "昵称不能为空")
    @Size(min = 3, max = 50, message = "昵称长度必须在4-50个字符之间")
    private  String  nickname ;

    @Schema(description = "邮箱", example = "test@gmail.com")
    @Email(message = "邮箱格式不正确")
    private String email;


    public UserInfo toAccount() {
        return  this.to(UserInfo.class);
    }
}
