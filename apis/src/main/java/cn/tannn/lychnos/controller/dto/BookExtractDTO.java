package cn.tannn.lychnos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 书籍提取请求
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
@Getter
@Setter
@ToString
@Schema(description = "书籍提取请求")
public class BookExtractDTO {
    /**
     * 用户输入的书籍信息
     */
    @Schema(description = "用户输入的书籍信息", example = "三体 刘慈欣")
    private String input;
}
