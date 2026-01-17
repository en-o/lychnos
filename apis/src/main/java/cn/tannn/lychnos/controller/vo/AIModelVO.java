package cn.tannn.lychnos.controller.vo;

import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.entity.AIModel;
import cn.tannn.lychnos.service.AIModelService;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * AI 模型 VO
 * 用于返回给前端，包含掩码后的 API Key
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/17
 */
@Schema(description = "AI模型VO")
@Getter
@Setter
@ToString
@Builder
public class AIModelVO {

    @Schema(description = "主键ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "配置命名")
    private String name;

    @Schema(description = "模型名")
    private String model;

    @Schema(description = "api厂家")
    private String factory;

    @Schema(description = "apiKey（掩码或空提示）")
    private String apiKey;

    @Schema(description = "apiUrl")
    private String apiUrl;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "模型类型")
    private ModelType type;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 从 Entity 转换为 VO
     *
     * @param model AI 模型实体
     * @param aiModelService AI 模型服务（用于掩码 API Key）
     * @return AI 模型 VO
     */
    public static AIModelVO fromEntity(AIModel model, AIModelService aiModelService) {
        String maskedApiKey = aiModelService.getMaskedApiKey(model.getApiKey());

        return AIModelVO.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .name(model.getName())
                .model(model.getModel())
                .factory(model.getFactory())
                .apiKey(maskedApiKey)
                .apiUrl(model.getApiUrl())
                .enabled(model.getEnabled())
                .type(model.getType())
                .createTime(model.getCreateTime())
                .updateTime(model.getUpdateTime())
                .build();
    }
}
