package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.controller.vo.LoginVO;
import cn.tannn.lychnos.controller.vo.OAuth2ProviderVO;
import cn.tannn.lychnos.entity.OAuthConfig;
import cn.tannn.lychnos.enums.ProviderType;
import cn.tannn.lychnos.service.OAuth2Service;
import cn.tannn.lychnos.service.OAuthConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OAuth2 第三方登录控制器
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@PathRestController("oauth")
@Tag(name = "第三方登录管理")
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginController {

    private final OAuthConfigService oauthConfigService;
    private final OAuth2Service oauth2Service;

    /**
     * 获取所有第三方登录平台
     *
     * @return 第三方登录平台列表
     */
    @Operation(summary = "获取所有第三方登录平台")
    @ApiMapping(value = "/providers", checkToken = false, method = RequestMethod.GET)
    public ResultVO<List<OAuth2ProviderVO>> getProviders() {
        List<OAuthConfig> configs = oauthConfigService.getEnabledConfigs();

        List<OAuth2ProviderVO> providers = configs.stream()
                .map(config -> {
                    OAuth2ProviderVO vo = new OAuth2ProviderVO();
                    vo.setType(config.getProviderType().getValue());
                    vo.setName(config.getProviderType().getDisplayName());
                    vo.setIconUrl(config.getIconUrl());
                    vo.setAuthorizeUrl(config.getAuthorizeUrl());
                    vo.setSortOrder(config.getSortOrder());
                    return vo;
                })
                .collect(Collectors.toList());

        return ResultVO.success(providers);
    }

    /**
     * 生成第三方登录授权URL
     *
     * @param providerType 平台类型（GITHUB, LINUXDO 等）
     * @return 授权URL
     */
    @Operation(summary = "生成第三方登录授权URL")
    @ApiMapping(value = "/authorize/{providerType}", checkToken = false, method = RequestMethod.GET)
    public ResultVO<String> getAuthorizeUrl(@PathVariable String providerType) {
        // 生成随机 state（用于防CSRF攻击）
        String state = UUID.randomUUID().toString();

        // 转换为枚举
        ProviderType providerTypeEnum = ProviderType.fromValue(providerType);

        // 生成授权URL
        String authorizeUrl = oauth2Service.generateAuthorizeUrl(providerTypeEnum, state);

        log.info("生成授权URL成功：平台={}, state={}", providerType, state);
        return ResultVO.success(authorizeUrl);
    }

    /**
     * 处理第三方登录回调
     *
     * @param providerType 平台类型
     * @param code         授权码
     * @param state        状态码（防CSRF）
     * @return 登录Token
     */
    @Operation(summary = "处理第三方登录回调")
    @ApiMapping(value = "/callback/{providerType}", checkToken = false, method = RequestMethod.GET)
    public ResultVO<LoginVO> handleCallback(
            @PathVariable String providerType,
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        log.info("收到OAuth2回调：平台={}, code存在={}, state={}", providerType, (code != null), state);

        // TODO: 验证 state（需要前端将 state 存储到 localStorage 或 SessionStorage）
        // 当前简化实现，省略 state 验证

        // 转换为枚举
        ProviderType providerTypeEnum = ProviderType.fromValue(providerType);

        LoginVO loginVO = oauth2Service.handleCallback(providerTypeEnum, code, state);

        return ResultVO.success("登录成功", loginVO);
    }
}
