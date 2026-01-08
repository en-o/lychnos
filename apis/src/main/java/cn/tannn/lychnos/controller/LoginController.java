package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.jwt.standalone.service.LoginService;
import cn.tannn.jdevelops.jwt.standalone.util.JwtWebUtil;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.jdevelops.utils.jwt.module.SignEntity;
import cn.tannn.lychnos.controller.dto.LoginPassword;
import cn.tannn.lychnos.controller.vo.LoginVO;
import cn.tannn.lychnos.entity.UserInfo;
import cn.tannn.lychnos.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 登录
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/8 22:04
 */
@PathRestController("")
@Tag(name = "登录管理")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final UserInfoService userInfoService;
    private final LoginService loginService;
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
        UserInfo userInfo = userInfoService.authenticateUser(login);
        String sign = loginUserSign(userInfo, request);
        return ResultVO.success("登录成功", new LoginVO(sign));
    }


    /**
     * 退出
     *
     * @param request HttpServletRequest
     * @return 退出
     */
    @Operation(summary = "退出")
    @GetMapping("/logout")
    public ResultVO<String> logout(HttpServletRequest request) {
        // 当前jwt没有退出一说，后续加一个map存储不信任token列表
        return ResultVO.successMessage("成功退出");
    }


    @Operation(summary = "解析当前登录者的token")
    @ApiMapping(value = "parse")
    public ResultVO<SignEntity<String>> parseToken(HttpServletRequest request) {
        return ResultVO.success(JwtWebUtil.getTokenBySignEntity(request));
    }


    /**
     * 构造登录信息
     *
     * @param account  UserInfo
     * @param request  HttpServletRequest
     * @return token
     */
    private String loginUserSign(UserInfo account, HttpServletRequest request) {
        SignEntity<String> init = SignEntity.init(account.getLoginName());
        return loginService.login(init).getSign();
    }


}
