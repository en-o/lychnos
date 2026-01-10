package cn.tannn.lychnos.controller.dto;

import cn.tannn.jdevelops.result.bean.SerializableBean;
import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.entity.AIModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI模型DTO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/10 11:00
 */
@Schema(description = "AI模型DTO")
@ToString
@Getter
@Setter
public class AIModelDTO extends SerializableBean<AIModelDTO> {

    /**
     * 配置命名
     */
    @Schema(description = "配置命名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置命名不允许为空")
    private String name;

    /**
     * 模型名
     */
    @Schema(description = "模型名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模型名不允许为空")
    private String model;

    /**
     * api厂家
     */
    @Schema(description = "api厂家:openai|ollama|deepseek|azure|anthropic|qwen|baidu|自定义", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "api厂家不允许为空")
    private String factory;

    /**
     * apiKey
     */
    @Schema(description = "apiKey")
    private String apiKey;

    /**
     * apiUrl
     */
    @Schema(description = "apiUrl", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "apiUrl不允许为空")
    private String apiUrl;

    /**
     * 模型类型
     */
    @Schema(description = "模型类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模型类型不允许为空")
    private ModelType type;

    /**
     * 转换为实体
     */
    public AIModel toEntity(Long userId) {
        AIModel model = new AIModel();
        model.setUserId(userId);
        model.setName(this.name);
        model.setModel(this.model);
        model.setFactory(this.factory);
        model.setApiKey(this.apiKey);
        model.setApiUrl(this.apiUrl);
        model.setEnabled(false);
        model.setType(this.type);
        return model;
    }

    /**
     * 更新实体
     */
    public void updateEntity(AIModel model) {
        model.setName(this.name);
        model.setModel(this.model);
        model.setFactory(this.factory);
        model.setApiKey(this.apiKey);
        model.setApiUrl(this.apiUrl);
        model.setType(this.type);
    }
}
