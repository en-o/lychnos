package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.dao.OAuthConfigDao;
import cn.tannn.lychnos.entity.OAuthConfig;
import cn.tannn.lychnos.enums.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * OAuth2 配置管理服务
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Service
@Slf4j
public class OAuthConfigService extends J2ServiceImpl<OAuthConfigDao, OAuthConfig, Long> {

    public OAuthConfigService() {
        super(OAuthConfig.class);
    }

    /**
     * 获取所有平台配置
     *
     * @return 平台配置列表
     */
    /**
     * 获取所有平台配置
     *
     * @return 平台配置列表
     */
    public List<OAuthConfig> getAllConfigs() {
        return getJpaBasicsDao().findAllByOrderBySortOrder();
    }

    /**
     * 获取所有启用的平台配置
     *
     * @return 启用的平台配置列表
     */
    public List<OAuthConfig> getEnabledConfigs() {
        return getJpaBasicsDao().findAllByEnabledTrueOrderBySortOrder();
    }

    /**
     * 根据平台类型获取配置
     *
     * @param providerType 平台类型
     * @return OAuth2配置
     */
    public Optional<OAuthConfig> getConfigByType(ProviderType providerType) {
        return getJpaBasicsDao().findByProviderType(providerType);
    }

    /**
     * 保存或更新配置
     *
     * @param config OAuth2配置
     * @return 保存后的配置
     */
    public OAuthConfig saveConfig(OAuthConfig config) {
        return getJpaBasicsDao().save(config);
    }
}
