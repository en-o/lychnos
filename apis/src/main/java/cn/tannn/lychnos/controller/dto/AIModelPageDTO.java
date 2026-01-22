package cn.tannn.lychnos.controller.dto;

import cn.tannn.jdevelops.annotations.jpa.JpaSelectIgnoreField;
import cn.tannn.jdevelops.util.jpa.request.PagingSorteds;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI模型分页查询
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/22
 */
@ToString
@Getter
@Setter
public class AIModelPageDTO {

    @Schema(description = "用户名（模糊查询）")
    private String loginName;

    @Schema(description = "昵称（模糊查询）")
    private String nickname;

    @Schema(description = "模型名（模糊查询）")
    @JpaSelectIgnoreField
    private String model;

    @Schema(description = "分页排序")
    @JpaSelectIgnoreField
    @Valid
    private PagingSorteds page;

    public PagingSorteds getPage() {
        if (page == null) {
            return new PagingSorteds().fixSort(1, "createTime");
        }
        return page;
    }
}
