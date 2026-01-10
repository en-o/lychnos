package cn.tannn.lychnos.dao;

import cn.tannn.jdevelops.jpa.repository.JpaBasicsRepository;
import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.entity.AIModel;

import java.util.List;

/**
 * ai模型配置
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/10 10:25
 */
public interface AIModelDao extends JpaBasicsRepository<AIModel, Long> {

    /**
     * 根据用户ID和模型类型查询，按启用状态和创建时间倒序
     */
    List<AIModel> findByUserIdAndTypeOrderByEnabledDescCreateTimeDesc(Long userId, ModelType type);

    /**
     * 根据用户ID、模型类型和启用状态查询
     */
    List<AIModel> findByUserIdAndTypeAndEnabled(Long userId, ModelType type, Boolean enabled);
}
