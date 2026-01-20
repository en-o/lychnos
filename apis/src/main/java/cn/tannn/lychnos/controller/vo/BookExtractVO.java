package cn.tannn.lychnos.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 书籍提取结果
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
@Getter
@Setter
@ToString
@Schema(description = "书籍提取结果")
public class BookExtractVO {
    /**
     * 书名
     */
    @Schema(description = "书名",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "书名不允许为空")
    private String title;

    /**
     * 作者
     */
    @Schema(description = "作者")
    private String author;

    /**
     * 是否已分析过
     */
    @Schema(description = "是否已分析过")
    private Boolean analyzed;

    /**
     * 书籍来源类型
     * USER_INPUT: 用户输入的书籍（AI识别出的）
     * SIMILAR: 相似推荐书籍
     * NOT_FOUND_RECOMMEND: 未找到时的推荐书籍
     */
    @Schema(description = "书籍来源类型：USER_INPUT-用户输入, SIMILAR-相似推荐, NOT_FOUND_RECOMMEND-未找到推荐")
    private String sourceType;

    /**
     * 来源说明（用于前端显示提示信息）
     */
    @Schema(description = "来源说明")
    private String sourceLabel;

    public BookExtractVO() {
    }

    public BookExtractVO(String title, String author) {
        this.title = title;
        this.author = author;
        this.analyzed = false;
        this.sourceType = "USER_INPUT";
    }

    public BookExtractVO(String title, String author, Boolean analyzed) {
        this.title = title;
        this.author = author;
        this.analyzed = analyzed;
        this.sourceType = "USER_INPUT";
    }

    public BookExtractVO(String title, String author, Boolean analyzed, String sourceType, String sourceLabel) {
        this.title = title;
        this.author = author;
        this.analyzed = analyzed;
        this.sourceType = sourceType;
        this.sourceLabel = sourceLabel;
    }
}
