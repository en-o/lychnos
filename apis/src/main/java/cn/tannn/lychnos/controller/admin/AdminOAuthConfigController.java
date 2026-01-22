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
import org.apache.commons.lang3.StringUtils;
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
     * 新增OAuth配置
     */
    @Operation(summary = "新增OAuth配置")
    @ApiMapping(value = "/create", method = RequestMethod.POST)
    public ResultVO<Void> createConfig(@RequestBody OAuthConfigDTO dto, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        // 验证必填字段
        if (StringUtils.isBlank(dto.getProviderType())) {
            throw new RuntimeException("平台类型不能为空");
        }
        if (StringUtils.isBlank(dto.getClientId())) {
            throw new RuntimeException("Client ID不能为空");
        }
        if (StringUtils.isBlank(dto.getClientSecret())) {
            throw new RuntimeException("Client Secret不能为空");
        }
        if (StringUtils.isBlank(dto.getAuthorizeUrl())) {
            throw new RuntimeException("授权端点不能为空");
        }
        if (StringUtils.isBlank(dto.getTokenUrl())) {
            throw new RuntimeException("Token端点不能为空");
        }
        if (StringUtils.isBlank(dto.getUserInfoUrl())) {
            throw new RuntimeException("用户信息端点不能为空");
        }
        if (StringUtils.isBlank(dto.getWebCallbackUrl())) {
            throw new RuntimeException("Web回调地址前缀不能为空");
        }

        OAuthConfig config = new OAuthConfig();
        config.setProviderType(cn.tannn.lychnos.enums.ProviderType.fromValue(dto.getProviderType().trim()));
        config.setClientId(dto.getClientId().trim());
        config.setClientSecret(dto.getClientSecret().trim());
        config.setAuthorizeUrl(dto.getAuthorizeUrl().trim());
        config.setTokenUrl(dto.getTokenUrl().trim());
        config.setUserInfoUrl(dto.getUserInfoUrl().trim());
        config.setWebCallbackUrl(dto.getWebCallbackUrl().trim());
        config.setScope(StringUtils.isNotBlank(dto.getScope()) ? dto.getScope().trim() : "");
        config.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        config.setEnabled(false); // 默认停用

        // 可选字段：iconUrl
        if (StringUtils.isNotBlank(dto.getIconUrl())) {
            config.setIconUrl(dto.getIconUrl().trim());
        }

        oauthConfigService.saveConfig(config);
        log.info("管理员新增OAuth配置：{}", config.getProviderType());

        return ResultVO.successMessage("新增成功");
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

        // 验证必填字段（编辑时这些字段也必须提供）
        if (StringUtils.isBlank(dto.getClientId())) {
            throw new RuntimeException("Client ID不能为空");
        }
        if (StringUtils.isBlank(dto.getAuthorizeUrl())) {
            throw new RuntimeException("授权端点不能为空");
        }
        if (StringUtils.isBlank(dto.getTokenUrl())) {
            throw new RuntimeException("Token端点不能为空");
        }
        if (StringUtils.isBlank(dto.getUserInfoUrl())) {
            throw new RuntimeException("用户信息端点不能为空");
        }
        if (StringUtils.isBlank(dto.getWebCallbackUrl())) {
            throw new RuntimeException("Web回调地址前缀不能为空");
        }

        // 更新必填字段
        config.setClientId(dto.getClientId().trim());
        config.setAuthorizeUrl(dto.getAuthorizeUrl().trim());
        config.setTokenUrl(dto.getTokenUrl().trim());
        config.setUserInfoUrl(dto.getUserInfoUrl().trim());
        config.setWebCallbackUrl(dto.getWebCallbackUrl().trim());

        // Client Secret：留空则不修改
        if (StringUtils.isNotBlank(dto.getClientSecret())) {
            config.setClientSecret(dto.getClientSecret().trim());
        }

        // Scope：可选字段
        if (StringUtils.isNotBlank(dto.getScope())) {
            config.setScope(dto.getScope().trim());
        }

        // IconUrl：可选字段
        if (StringUtils.isNotBlank(dto.getIconUrl())) {
            config.setIconUrl(dto.getIconUrl().trim());
        }

        // SortOrder：可选字段
        if (dto.getSortOrder() != null) {
            config.setSortOrder(dto.getSortOrder());
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
     * 更新OAuth配置排序
     */
    @Operation(summary = "更新OAuth配置排序")
    @ApiMapping(value = "/update-sort/{id}/{sortOrder}", method = RequestMethod.PUT)
    public ResultVO<Void> updateSortOrder(@PathVariable Long id, @PathVariable Integer sortOrder, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        OAuthConfig config = oauthConfigService.getJpaBasicsDao().findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在"));

        config.setSortOrder(sortOrder);
        oauthConfigService.saveConfig(config);

        log.info("管理员更新OAuth配置排序：{} -> {}", config.getProviderType(), sortOrder);
        return ResultVO.successMessage("排序更新成功");
    }

    /**
     * 删除OAuth配置
     */
    @Operation(summary = "删除OAuth配置")
    @ApiMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public ResultVO<Void> deleteConfig(@PathVariable Long id, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        OAuthConfig config = oauthConfigService.getJpaBasicsDao().findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在"));

        // 检查是否为启用状态
        if (config.getEnabled()) {
            throw new RuntimeException("启用状态的配置不允许删除，请先停用");
        }

        oauthConfigService.getJpaBasicsDao().deleteById(id);
        log.info("管理员删除OAuth配置：{}", config.getProviderType());

        return ResultVO.successMessage("删除成功");
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
