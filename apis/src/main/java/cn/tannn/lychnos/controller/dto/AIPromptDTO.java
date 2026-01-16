package cn.tannn.lychnos.controller.dto;

import cn.tannn.jdevelops.result.bean.SerializableBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 提示词 DTO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
@Schema(description = "AI 提示词请求")
@ToString
@Getter
@Setter
public class AIPromptDTO extends SerializableBean<AIPromptDTO> {

    /**
     * 提示词
     */
    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "一只可爱的小猫在草地上玩耍")
    @NotBlank(message = "提示词不允许为空")
    private String prompt;
}
