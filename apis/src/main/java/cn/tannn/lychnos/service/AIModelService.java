package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.dao.AIModelDao;
import cn.tannn.lychnos.entity.AIModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * ai模型配置
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/10 10:26
 */
@Service
@Slf4j
public class AIModelService extends J2ServiceImpl<AIModelDao, AIModel, Long> {
    public AIModelService() {
        super(AIModel.class);
    }

    /**
     * 根据用户ID和模型类型查询
     */
    public List<AIModel> findByUserIdAndType(Long userId, ModelType type) {
        return getJpaBasicsDao().findByUserIdAndTypeOrderByEnabledDescCreateTimeDesc(userId, type);
    }

    /**
     * 禁用用户的所有同类型模型
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableAllByUserIdAndType(Long userId, ModelType type) {
        List<AIModel> models = getJpaBasicsDao().findByUserIdAndTypeAndEnabled(userId, type, true);
        models.forEach(model -> model.setEnabled(false));
        getJpaBasicsDao().saveAll(models);
    }

    /**
     * 查询模型
     *
     * @param id 模型id
     * @return AIModel
     */
    public AIModel findById(Long id) {
        return getJpaBasicsDao().findById(id).orElseThrow(() -> new BusinessException("模型不存在"));
    }

    /**
     * 查询模型并验证权限
     *
     * @param id 模型id
     * @return AIModel
     */
    public AIModel findVerifyRole(Long id, Long userId) {
        AIModel aiModel = getJpaBasicsDao().findById(id).orElseThrow(() -> new BusinessException("模型不存在"));

        // 验证是否是当前用户的模型
        if (!Objects.equals(aiModel.getUserId(), userId)) {
            throw new IllegalArgumentException("无权限操作");
        }
        return aiModel;
    }
}
