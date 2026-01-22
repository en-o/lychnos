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
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
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
                    vo.setSortOrder(config.getSortOrder());
                    return vo;
                })
                .collect(Collectors.toList());

        return ResultVO.success(providers);
    }

    /**
     * 生成第三方登录授权URL
     * <p>
     * 此接口支持两种场景：
     * 1. 第三方登录：不传 loginName 参数，用户通过第三方平台登录系统
     * 2. 绑定第三方账户：传入 loginName 参数，将第三方账户绑定到指定用户
     * </p>
     * <p>
     * 配置示例：
     * - GitHub: https://github.com/settings/applications/3348764
     *   回调地址: http://localhost:1250/oauth/callback/github (注意大小写敏感)
     * - LinuxDo: https://connect.linux.do/dash/sso
     *   回调地址: http://localhost:1250/oauth/callback/LINUXDO
     * </p>
     *
     * @param providerType 平台类型（GITHUB, LINUXDO 等）
     * @param loginName    当前登录用户名（可选，用于绑定场景）
     * @return 授权URL
     */
    @Operation(summary = "生成第三方登录授权URL")
    @ApiMapping(value = "/authorize/{providerType}", checkToken = false, method = RequestMethod.GET)
    public ResultVO<String> getAuthorizeUrl(@PathVariable String providerType,
            @RequestParam(required = false) String loginName) {
        // 转换为枚举
        ProviderType providerTypeEnum = ProviderType.fromValue(providerType);

        // 生成授权URL (state 由 service 内部生成和加密)
        String authorizeUrl = oauth2Service.generateAuthorizeUrl(providerTypeEnum, loginName);

        log.info("生成授权URL成功：平台={}, 绑定用户={}", providerType, loginName);
        return ResultVO.success(authorizeUrl);
    }

    /**
     * 处理第三方登录回调
     * <p>
     * 此接口统一处理第三方平台的授权回调，支持两种场景：
     * 1. 第三方登录：创建新用户或登录已有用户
     * 2. 绑定第三方账户：将第三方账户绑定到指定用户
     * </p>
     * <p>
     * 场景判断：通过解析 state 参数中的加密信息判断是登录还是绑定操作
     * - LOGIN: 第三方登录场景
     * - BIND: 绑定第三方账户场景
     * </p>
     * <p>
     * 回调流程：
     * 1. 第三方平台验证用户授权后，携带 code 和 state 参数回调此接口
     * 2. 后端使用 code 换取 access_token
     * 3. 使用 access_token 获取第三方用户信息
     * 4. 根据 state 判断执行登录或绑定操作
     * 5. 重定向到前端页面，携带 token 参数
     * </p>
     *
     * @param providerType 平台类型
     * @param code         授权码
     * @param state        状态码（防CSRF，包含操作类型和用户信息）
     * @return 重定向到Web主页
     */
    @Operation(summary = "处理第三方登录回调")
    @ApiMapping(value = "/callback/{providerType}", checkToken = false, method = RequestMethod.GET)
    public RedirectView handleCallback(
            @PathVariable String providerType,
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        log.info("收到OAuth2回调：平台={}, code存在={}, state={}", providerType, (code != null), state);

        // 转换为枚举
        ProviderType providerTypeEnum = ProviderType.fromValue(providerType);

        // 处理OAuth2回调，获取登录信息
        LoginVO loginVO = oauth2Service.handleCallback(providerTypeEnum, code, state);

        // 获取配置的Web回调地址前缀
        OAuthConfig config = oauthConfigService.getConfigByProviderType(providerTypeEnum);
        String webCallbackPrefix = config.getWebCallbackUrl();

        // 如果没有配置Web回调地址前缀，使用空字符串（相对路径）
        if (webCallbackPrefix == null || webCallbackPrefix.isEmpty()) {
            webCallbackPrefix = "";
        }

        // 移除末尾的斜杠（如果有）
        if (webCallbackPrefix.endsWith("/")) {
            webCallbackPrefix = webCallbackPrefix.substring(0, webCallbackPrefix.length() - 1);
        }

        // 构建完整的重定向URL
        // 格式：{前缀}#/oauth/callback?token={token}
        // 示例：http://localhost:3000/lychnos#/oauth/callback?token=xxx
        String redirectUrl = String.format("%s#/oauth/callback?token=%s",
            webCallbackPrefix,
            loginVO.getToken());

        log.info("OAuth2登录成功，重定向到：{}", redirectUrl);
        return new RedirectView(redirectUrl);
    }
}
