package cn.tannn.lychnos.controller.vo;

import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.entity.AIModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 官方模型VO - 仅用于展示
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "官方模型VO")
public class OfficialModelVO {

    /**
     * 模型类型
     */
    @Schema(description = "模型类型")
    private ModelType type;

    /**
     * 模型名
     */
    @Schema(description = "模型名")
    private String model;

    /**
     * 从实体转换为VO
     *
     * @param entity 实体对象
     * @return OfficialModelVO
     */
    public static OfficialModelVO fromEntity(AIModel entity) {
        if (entity == null) {
            return null;
        }
        return OfficialModelVO.builder()
                .type(entity.getType())
                .model(entity.getModel())
                .build();
    }
}
