package cn.tannn.lychnos.dao;

import cn.tannn.jdevelops.jpa.repository.JpaBasicsRepository;
import cn.tannn.lychnos.entity.OAuthConfig;
import cn.tannn.lychnos.enums.ProviderType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OAuth2 配置 DAO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Repository
public interface OAuthConfigDao extends JpaBasicsRepository<OAuthConfig, Long> {

    /**
     * 根据平台类型查询配置
     *
     * @param providerType 平台类型（如 GITHUB、LINUXDO）
     * @return OAuth2配置
     */
    Optional<OAuthConfig> findByProviderType(ProviderType providerType);

    /**
     * 查询所有平台配置，按排序顺序返回
     *
     * @return 平台配置列表
     */
    List<OAuthConfig> findAllByOrderBySortOrder();

    /**
     * 获取所有启用的配置，按排序顺序排列
     *
     * @return 启用的配置列表
     */
    List<OAuthConfig> findAllByEnabledTrueOrderBySortOrder();
}
