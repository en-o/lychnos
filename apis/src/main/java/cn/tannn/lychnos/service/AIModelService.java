package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.common.util.AESUtil;
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

    /**
     * 解密 API Key（用于实际调用 AI 接口）
     *
     * @param encryptedApiKey 加密后的 API Key
     * @return 解密后的 API Key
     */
    public String decryptApiKey(String encryptedApiKey) {
        if (encryptedApiKey == null || encryptedApiKey.isEmpty()) {
            return encryptedApiKey;
        }
        try {
            return AESUtil.decrypt(encryptedApiKey);
        } catch (Exception e) {
            log.error("API Key 解密失败", e);
            throw new BusinessException("API Key 解密失败");
        }
    }

    /**
     * 将 API Key 掩码显示（用于返回给前端）
     * 不修改原对象，返回掩码后的字符串
     *
     * @param apiKey 加密的 API Key
     * @return 掩码后的字符串
     */
    public String getMaskedApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return ""; // 返回空字符串，前端会显示提示
        }
        try {
            // 先解密，再掩码
            String decrypted = AESUtil.decrypt(apiKey);
            return AESUtil.maskText(decrypted);
        } catch (Exception e) {
            log.error("API Key 处理失败", e);
            // 处理失败时显示为掩码
            return "****";
        }
    }

    /**
     * 将 API Key 掩码显示（用于返回给前端）
     * ⚠️ 注意：此方法会修改 model 对象，不要在事务内使用
     *
     * @param model AI 模型
     */
    @Deprecated
    public void maskApiKey(AIModel model) {
        if (model.getApiKey() != null && !model.getApiKey().isEmpty()) {
            model.setApiKey(getMaskedApiKey(model.getApiKey()));
        }
    }

    /**
     * 批量掩码 API Key
     * ⚠️ 注意：此方法会修改 model 对象，不要在事务内使用
     *
     * @param models AI 模型列表
     */
    @Deprecated
    public void maskApiKeys(List<AIModel> models) {
        models.forEach(this::maskApiKey);
    }
}
