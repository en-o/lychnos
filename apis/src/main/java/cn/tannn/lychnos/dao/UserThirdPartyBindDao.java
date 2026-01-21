package cn.tannn.lychnos.dao;

import cn.tannn.jdevelops.jpa.repository.JpaBasicsRepository;
import cn.tannn.lychnos.entity.UserThirdPartyBind;
import cn.tannn.lychnos.enums.ProviderType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户第三方账户绑定 DAO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Repository
public interface UserThirdPartyBindDao extends JpaBasicsRepository<UserThirdPartyBind, Long> {

    /**
     * 根据平台类型和OpenID查询绑定关系
     *
     * @param providerType 平台类型
     * @param openId       第三方平台用户唯一标识
     * @return 绑定关系
     */
    Optional<UserThirdPartyBind> findByProviderTypeAndOpenId(ProviderType providerType, String openId);

    /**
     * 根据用户ID查询所有绑定关系
     *
     * @param userId 用户ID
     * @return 绑定关系列表
     */
    List<UserThirdPartyBind> findByUserId(Long userId);

    /**
     * 根据用户ID和平台类型查询绑定关系
     *
     * @param userId       用户ID
     * @param providerType 平台类型
     * @return 绑定关系
     */
    Optional<UserThirdPartyBind> findByUserIdAndProviderType(Long userId, ProviderType providerType);

    boolean existsByUserIdAndProviderType(Long userId, ProviderType providerType);
}
