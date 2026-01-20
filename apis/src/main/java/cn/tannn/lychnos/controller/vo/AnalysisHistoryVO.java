package cn.tannn.lychnos.controller.vo;

import cn.tannn.lychnos.entity.BookAnalyse;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 分析历史（包含完整分析数据）
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 23:00
 */
@Schema(description = "分析历史")
@ToString
@Getter
@Setter
public class AnalysisHistoryVO {

    @Schema(description = "用户兴趣ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "书名")
    private String title;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "是否感兴趣")
    private Boolean interested;

    @Schema(description = "创建时间")
    private String createTime;
}
