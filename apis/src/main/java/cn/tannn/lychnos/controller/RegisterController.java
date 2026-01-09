package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.constant.DefLoginName;
import cn.tannn.lychnos.controller.dto.UserInfoRegister;
import cn.tannn.lychnos.entity.UserInfo;
import cn.tannn.lychnos.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * 注册管理
 *
 * @author <a href="https://tannn.cn/">tan</a>
 * @date 2023/10/26 11:51
 */

@Tag(name = "注册管理", description = "注册管理",
        extensions = {
                @Extension(properties = {@ExtensionProperty(name = "x-order", value = "2", parseValue = true)})}
)
@PathRestController("register")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RegisterController {

    private final UserInfoService userInfoService;


    @Operation(summary = "用户注册", description = "默认")
    @ApiMapping(value = "myself", method = RequestMethod.POST, checkToken = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<String> myself(@RequestBody @Valid UserInfoRegister register, HttpServletRequest request) {
//        if (DefLoginName.SUPPER_USER.contains(register.getLoginName().toLowerCase())) {
//            return ResultVO.failMessage("非法注册用户");
//        }
        UserInfo registerAccount = register.toAccount();
        userInfoService.registerUser(registerAccount);
        return ResultVO.successMessage("账户注册成功");
    }


}
