package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.common.views.Views;
import cn.tannn.lychnos.controller.dto.PasswordEdit;
import cn.tannn.lychnos.controller.dto.UserInfoFix;
import cn.tannn.lychnos.controller.dto.UserInterestFeedback;
import cn.tannn.lychnos.entity.UserInfo;
import cn.tannn.lychnos.service.UserInfoService;
import cn.tannn.lychnos.service.UserInterestService;
import com.fasterxml.jackson.annotation.JsonView;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 用户
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/8 22:50
 */
@Tag(name = "用户", description = "用户",
        extensions = {
                @Extension(properties = {@ExtensionProperty(name = "x-order", value = "3", parseValue = true)})}
)
@PathRestController("user")
@Slf4j
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserInfoService userInfoService;
    private final UserInterestService userInterestService;


    @Operation(summary = "当前登录者信息")
    @GetMapping(value = "info")
    @JsonView(Views.Public.class)
    public ResultVO<UserInfo> userInfo(HttpServletRequest request) {
        UserInfo account = userInfoService.findByLoginName(UserUtil.loginName(request))
                .orElseThrow(() -> new BusinessException("获取用户失败，请尝试重新登录"));
        return ResultVO.success(account);
    }

    @Operation(summary = "修改基础信息")
    @PostMapping("fixInfo")
    @JsonView(Views.Public.class)
    public ResultVO<UserInfo> edit(@RequestBody @Valid UserInfoFix fix, HttpServletRequest request) {
        UserInfo account = userInfoService.updateInfo(fix);
        return ResultVO.success("修改成功", account);
    }

    /**
     * 修改密码 - 旧密码修改
     */
    @ApiMapping(value = "/fix/password", method = RequestMethod.POST)
    @Operation(summary = "修改密码-旧密码修改")
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<String> fixPassword(@RequestBody @Valid PasswordEdit password, HttpServletRequest request) {
        String loginName = UserUtil.loginName(request);
        userInfoService.editPassword(loginName, password);
        return ResultVO.successMessage("密码修改成功，请妥善保管新密码");
    }


    /**
     * 提交用户反馈（创建用户兴趣）
     */
    @PostMapping(value = "/interest")
    @Operation(summary = "提交用户反馈（创建用户兴趣）")
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<String> interest(@RequestBody @Valid UserInterestFeedback interest, HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);
        userInterestService.feedback(interest, userId);
        return ResultVO.successMessage("反馈已提交！");
    }
}
