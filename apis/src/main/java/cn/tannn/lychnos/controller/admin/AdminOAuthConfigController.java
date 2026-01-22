package cn.tannn.lychnos.controller.admin;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.controller.dto.OAuthConfigDTO;
import cn.tannn.lychnos.controller.vo.OAuthConfigDetailVO;
import cn.tannn.lychnos.entity.OAuthConfig;
import cn.tannn.lychnos.service.OAuthConfigService;
import cn.tannn.lychnos.service.UserInfoService;
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
 * 管理员-OAuth配置管理
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/22
 */
@PathRestController("sys-manage/oauth-config")
@Tag(name = "系统管理-OAuth配置管理")
@RequiredArgsConstructor
@Slf4j
public class AdminOAuthConfigController {

    private final OAuthConfigService oauthConfigService;
    private final UserInfoService userInfoService;



    /**
     * 获取所有OAuth配置列表
     */
    @Operation(summary = "获取所有OAuth配置列表")
    @ApiMapping(value = "/list", method = RequestMethod.GET)
    public ResultVO<List<OAuthConfigDetailVO>> listConfigs(HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        List<OAuthConfig> configs = oauthConfigService.getAllConfigs();
        List<OAuthConfigDetailVO> vos = configs.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        return ResultVO.success(vos);
    }

    /**
     * 更新OAuth配置
     */
    @Operation(summary = "更新OAuth配置")
    @ApiMapping(value = "/update", method = RequestMethod.PUT)
    public ResultVO<Void> updateConfig(@RequestBody OAuthConfigDTO dto, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        OAuthConfig config = oauthConfigService.getJpaBasicsDao().findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("配置不存在"));

        // 更新字段
        if (dto.getClientId() != null) {
            config.setClientId(dto.getClientId());
        }
        if (dto.getClientSecret() != null) {
            config.setClientSecret(dto.getClientSecret());
        }
        if (dto.getAuthorizeUrl() != null) {
            config.setAuthorizeUrl(dto.getAuthorizeUrl());
        }
        if (dto.getTokenUrl() != null) {
            config.setTokenUrl(dto.getTokenUrl());
        }
        if (dto.getUserInfoUrl() != null) {
            config.setUserInfoUrl(dto.getUserInfoUrl());
        }
        if (dto.getScope() != null) {
            config.setScope(dto.getScope());
        }
        if (dto.getIconUrl() != null) {
            config.setIconUrl(dto.getIconUrl());
        }
        if (dto.getSortOrder() != null) {
            config.setSortOrder(dto.getSortOrder());
        }
        if (dto.getWebCallbackUrl() != null) {
            config.setWebCallbackUrl(dto.getWebCallbackUrl());
        }

        oauthConfigService.saveConfig(config);
        log.info("管理员更新OAuth配置：{}", config.getProviderType());

        return ResultVO.successMessage("更新成功");
    }

    /**
     * 启用/停用OAuth配置
     */
    @Operation(summary = "启用/停用OAuth配置")
    @ApiMapping(value = "/toggle/{id}", method = RequestMethod.PUT)
    public ResultVO<Void> toggleEnabled(@PathVariable Long id, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        OAuthConfig config = oauthConfigService.getJpaBasicsDao().findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在"));

        config.setEnabled(!config.getEnabled());
        oauthConfigService.saveConfig(config);

        log.info("管理员{}OAuth配置：{}", config.getEnabled() ? "启用" : "停用", config.getProviderType());
        return ResultVO.successMessage(config.getEnabled() ? "已启用" : "已停用");
    }

    /**
     * 转换为详情VO
     */
    private OAuthConfigDetailVO convertToDetailVO(OAuthConfig config) {
        OAuthConfigDetailVO vo = new OAuthConfigDetailVO();
        vo.setId(config.getId());
        vo.setProviderType(config.getProviderType().getValue());
        vo.setProviderName(config.getProviderType().getDisplayName());
        vo.setClientId(config.getClientId());
        vo.setAuthorizeUrl(config.getAuthorizeUrl());
        vo.setTokenUrl(config.getTokenUrl());
        vo.setUserInfoUrl(config.getUserInfoUrl());
        vo.setScope(config.getScope());
        vo.setIconUrl(config.getIconUrl());
        vo.setSortOrder(config.getSortOrder());
        vo.setEnabled(config.getEnabled());
        vo.setWebCallbackUrl(config.getWebCallbackUrl());
        vo.setCreateTime(config.getCreateTime() != null ? config.getCreateTime().toString() : null);
        vo.setUpdateTime(config.getUpdateTime() != null ? config.getUpdateTime().toString() : null);
        return vo;
    }
}
