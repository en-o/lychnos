package cn.tannn.lychnos.controller.dto;

import cn.tannn.jdevelops.result.bean.SerializableBean;
import cn.tannn.lychnos.entity.UserInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 修改基础信息
 *
 * @author <a href="https://tannn.cn/">tan</a>
 * @date 2023/11/21 9:56
 */
@Schema(description = "修改基础信息")
@ToString
@Getter
@Setter
public class UserInfoFix extends SerializableBean<UserInfoFix> {
    /**
     * 用户id
     */
    @Schema(description = "用户id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonSerialize(using = ToStringSerializer.class)
    Long id;

    /** 昵称 */
    @Schema(description = "昵称")
    private  String  nickname ;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;


    public void update(UserInfo account){
        if(nickname!=null){
            account.setNickname(nickname);
        }
        if(StringUtils.isNotBlank(email)){
            account.setEmail(email);
        }
    }

}
