package cn.tannn.lychnos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户分析日志查询DTO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/23
 */
@Data
@Schema(description = "用户分析日志查询DTO")
public class UserAnalysisLogQueryDTO {

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 用户名（支持模糊查询和精确查询）
     */
    @Schema(description = "用户名")
    private String userName;

    /**
     * 是否精确匹配用户名（true-精确匹配，false-模糊匹配）
     */
    @Schema(description = "是否精确匹配用户名")
    private Boolean exactMatch;
}
