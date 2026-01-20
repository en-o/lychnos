package cn.tannn.lychnos.controller.vo;

import cn.tannn.lychnos.common.constant.BookSourceType;
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
     */
    @Schema(description = "书籍来源类型")
    private BookSourceType sourceType;

    public BookExtractVO() {
    }

    public BookExtractVO(String title, String author) {
        this.title = title;
        this.author = author;
        this.analyzed = false;
        this.sourceType = BookSourceType.USER_INPUT;
    }

    public BookExtractVO(String title, String author, Boolean analyzed) {
        this.title = title;
        this.author = author;
        this.analyzed = analyzed;
        this.sourceType = BookSourceType.USER_INPUT;
    }

    public BookExtractVO(String title, String author, Boolean analyzed, BookSourceType sourceType) {
        this.title = title;
        this.author = author;
        this.analyzed = analyzed;
        this.sourceType = sourceType;
    }

    /**
     * 获取来源说明（从枚举中获取）
     */
    public String getSourceLabel() {
        return sourceType != null ? sourceType.getDefaultLabel() : null;
    }
}
