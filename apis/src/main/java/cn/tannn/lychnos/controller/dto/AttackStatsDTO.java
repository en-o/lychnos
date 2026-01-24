package cn.tannn.lychnos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 攻击统计响应DTO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @date 2026/1/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "攻击统计数据")
public class AttackStatsDTO {

    @Schema(description = "总攻击IP数")
    private Long totalIpCount;

    @Schema(description = "总攻击次数")
    private Long totalAttackCount;

    @Schema(description = "TOP攻击者列表")
    private List<AttackRecord> topAttackers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "攻击记录")
    public static class AttackRecord {
        @Schema(description = "攻击者IP")
        private String ip;

        @Schema(description = "攻击次数")
        private Long count;
    }
}
