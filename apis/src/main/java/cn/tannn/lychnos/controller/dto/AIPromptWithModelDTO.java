package cn.tannn.lychnos.controller.dto;

import cn.tannn.jdevelops.result.bean.SerializableBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 提示词（指定模型）DTO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
@Schema(description = "AI 提示词（指定模型）请求")
@ToString
@Getter
@Setter
public class AIPromptWithModelDTO extends SerializableBean<AIPromptWithModelDTO> {

    /**
     * 模型ID
     */
    @Schema(description = "模型ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型ID不允许为空")
    private Long modelId;

    /**
     * 提示词
     */
    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "一只可爱的小猫在草地上玩耍")
    @NotBlank(message = "提示词不允许为空")
    private String prompt;
}
