package cn.tannn.lychnos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 提交用户反馈（创建用户兴趣）
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 22:03
 */
@Schema(description = "提交用户反馈（创建用户兴趣）")
@ToString
@Getter
@Setter
public class UserInterestFeedback {

    @Schema(description = "书籍分析ID")
    private Long bookAnalyseId;

    @Schema(description = "书名")
    private String bookTitle;

    @Schema(description = "是否感兴趣")
    private Boolean interested;

    @Schema(description = "反馈原因")
    private String reason;
}
