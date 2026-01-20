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

    public BookExtractVO() {
    }

    public BookExtractVO(String title, String author) {
        this.title = title;
        this.author = author;
        this.analyzed = false;
    }

    public BookExtractVO(String title, String author, Boolean analyzed) {
        this.title = title;
        this.author = author;
        this.analyzed = analyzed;
    }
}
