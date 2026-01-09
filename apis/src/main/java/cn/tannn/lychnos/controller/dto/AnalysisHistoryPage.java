package cn.tannn.lychnos.controller.dto;

import cn.tannn.jdevelops.annotations.jpa.JpaSelectIgnoreField;
import cn.tannn.jdevelops.jdectemplate.sql.PageInfo;
import cn.tannn.jdevelops.util.jpa.request.PagingSorteds;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 分析查询
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 23:25
 */
@ToString
@Getter
@Setter
public class AnalysisHistoryPage {

    @Schema(description = "书名")
    @JpaSelectIgnoreField
    private String bookTitle;

    /**
     * 分页排序
     */
    @Schema(description = "分页排序")
    @JpaSelectIgnoreField
    @Valid
    private PagingSorteds page;


    public PagingSorteds getPage() {
        if (page == null) {
            return new PagingSorteds().fixSort(1, "createTime");
        }
        return page.append(1,"createTime");
    }
}
