package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.constant.PlatformConstant;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.jdevelops.utils.validation.account.Account;
import cn.tannn.lychnos.controller.dto.LoginPassword;
import cn.tannn.lychnos.controller.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.List;

/**
 * 登录
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/8 22:04
 */
@PathRestController("")
@Tag(name = "AI解读")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    /**
     * 登录-管理端登录
     *
     * @param login LoginDTO
     * @return ResultVO
     */
    @Operation(summary = "账户密码登录")
    @ApiMapping(value = "/login", checkToken = false, method = RequestMethod.POST)
    public ResultVO<LoginVO> login(@RequestBody @Valid LoginPassword login, HttpServletRequest request) throws IllegalAccessException {
        log.info("登录请求，登录名：{}", login.getLoginName());
        return ResultVO.success("登录成功", new LoginVO("token"));
    }

}
