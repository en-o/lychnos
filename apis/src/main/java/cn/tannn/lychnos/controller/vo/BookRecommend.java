package cn.tannn.lychnos.controller.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 书籍推荐
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 16:13
 */
@Getter
@Setter
@ToString
@Schema(description = "书籍推荐")
public class BookRecommend {
    /**
     * 书籍ID
     */
    @Schema(description = "书籍ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 书籍名
     */
    @Schema(description = "书籍名")
    private String title;

    public BookRecommend() {
    }

    public BookRecommend(Long id, String title) {
        this.id = id;
        this.title = title;
    }
}
