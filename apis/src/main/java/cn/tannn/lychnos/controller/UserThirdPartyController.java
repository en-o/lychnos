package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.dto.BindAccountDTO;
import cn.tannn.lychnos.controller.vo.UserThirdPartyBindVO;
import cn.tannn.lychnos.entity.UserThirdPartyBind;
import cn.tannn.lychnos.enums.ProviderType;
import cn.tannn.lychnos.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户第三方账户管理控制器
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@PathRestController("user/third-party")
@Tag(name = "用户第三方账户管理")
@RequiredArgsConstructor
@Slf4j
public class UserThirdPartyController {

    private final OAuth2Service oauth2Service;

    /**
     * 获取当前用户已绑定的第三方账户列表
     *
     * @param request HTTP请求
     * @return 绑定列表
     */
    @Operation(summary = "获取当前用户已绑定的第三方账户列表")
    @ApiMapping(value = "/bindings", method = RequestMethod.GET)
    public ResultVO<List<UserThirdPartyBindVO>> getBindings(HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);

        List<UserThirdPartyBind> bindings = oauth2Service.getUserBindings(userId);

        List<UserThirdPartyBindVO> vos = bindings.stream()
                .map(bind -> {
                    UserThirdPartyBindVO vo = new UserThirdPartyBindVO();
                    vo.setProviderType(bind.getProviderType().getValue());
                    vo.setNickname(bind.getNickname());
                    vo.setAvatarUrl(bind.getAvatarUrl());
                    vo.setEmail(bind.getEmail());
                    vo.setCreateTime(bind.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        return ResultVO.success(vos);
    }

    /**
     * 绑定第三方账户
     *
     * @param dto         绑定请求参数
     * @param httpRequest HTTP请求
     * @return 绑定结果
     */
    @Operation(summary = "绑定第三方账户")
    @ApiMapping(value = "/bind", method = RequestMethod.POST)
    public ResultVO<Void> bindAccount(@RequestBody BindAccountDTO dto, HttpServletRequest httpRequest) {
        Long userId = UserUtil.userId2(httpRequest);

        oauth2Service.bindThirdPartyAccount(userId, dto.getProviderType(), dto.getCode());

        log.info("绑定第三方账户成功：用户ID={}, 平台={}", userId, dto.getProviderType());
        return ResultVO.successMessage("绑定成功");
    }

    /**
     * 解绑第三方账户
     *
     * @param providerType 平台类型
     * @param request      HTTP请求
     * @return 解绑结果
     */
    @Operation(summary = "解绑第三方账户")
    @ApiMapping(value = "/unbind/{providerType}", method = RequestMethod.DELETE)
    public ResultVO<Void> unbindAccount(@PathVariable String providerType, HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);

        // 转换为枚举
        ProviderType providerTypeEnum = ProviderType.fromValue(providerType);

        oauth2Service.unbindThirdPartyAccount(userId, providerTypeEnum);

        log.info("解绑第三方账户成功：用户ID={}, 平台={}", userId, providerType);
        return ResultVO.successMessage("解绑成功");
    }
}
