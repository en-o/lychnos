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
     * 根据用户ID和模型类型查询，按启用状态降序、创建时间降序排列
     * 优先返回已启用的模型，相同启用状态下按创建时间倒序
     *
     * @param userId 用户ID
     * @param type 模型类型
     * @return 模型列表
     */
    List<AIModel> findByUserIdAndTypeOrderByEnabledDescCreateTimeDesc(Long userId, ModelType type);

    /**
     * 根据用户ID、模型类型和启用状态查询
     *
     * @param userId 用户ID
     * @param type 模型类型
     * @param enabled 是否启用
     * @return 模型列表
     */
    List<AIModel> findByUserIdAndTypeAndEnabled(Long userId, ModelType type, Boolean enabled);

    /**
     * 根据用户ID和模型类型查询所有模型
     *
     * @param userId 用户ID
     * @param type 模型类型
     * @return 模型列表
     */
    List<AIModel> findByUserIdAndType(Long userId, ModelType type);

    /**
     * 统计用户指定类型的启用模型数量
     *
     * @param userId 用户ID
     * @param type 模型类型
     * @param enabled 是否启用
     * @return 模型数量
     */
    long countByUserIdAndTypeAndEnabled(Long userId, ModelType type, Boolean enabled);

    /**
     * 检查用户是否拥有指定模型
     *
     * @param id 模型ID
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByIdAndUserId(Long id, Long userId);
}
