package cn.tannn.lychnos.controller.admin;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.controller.dto.AttackStatsDTO;
import cn.tannn.lychnos.controller.dto.UserAnalysisLogQueryDTO;
import cn.tannn.lychnos.controller.vo.UserAnalysisLogVO;
import cn.tannn.lychnos.service.AttackStatsCacheService;
import cn.tannn.lychnos.service.UserAnalysisLogService;
import cn.tannn.lychnos.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员-日志查询
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/23
 */
@PathRestController("sys-manage/logs")
@Tag(name = "系统管理-日志查询")
@RequiredArgsConstructor
@Slf4j
public class AdminLogController {

    private final UserAnalysisLogService userAnalysisLogService;
    private final UserInfoService userInfoService;
    private final AttackStatsCacheService attackStatsService;

    /**
     * 查询用户分析日志
     * 默认查询最近20条，支持时间范围和用户名查询，最多返回200条
     * 直接返回JPA Projection接口，无需手动转换
     */
    @Operation(summary = "查询用户分析日志")
    @ApiMapping(value = "/query", method = RequestMethod.POST)
    public ResultVO<List<UserAnalysisLogVO>> queryLogs(@RequestBody UserAnalysisLogQueryDTO queryDTO,
                                                       HttpServletRequest request) {
        // 鉴权：仅管理员可访问
        userInfoService.checkAdmin(request);

        // 查询日志 - 直接返回JPA Projection接口
        List<UserAnalysisLogVO> vos = userAnalysisLogService.queryLogs(queryDTO);

        log.info("管理员查询日志，查询条件: {}, 结果数量: {}", queryDTO, vos.size());
        return ResultVO.success(vos);
    }


    /**
     * 获取攻击统计数据
     *
     * @param limit 返回TOP数量，默认10，0表示全部
     * @return 攻击统计
     */
    @Operation(summary = "获取攻击统计", description = "查询当前时段（近1小时）的攻击统计数据")
    @ApiMapping(value = "/attack-stats", method = RequestMethod.GET)
    public ResultVO<AttackStatsDTO> getAttackStats(
            @Parameter(description = "返回TOP数量，0表示全部")
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        // 鉴权：仅管理员可访问
        userInfoService.checkAdmin(request);
        // 获取统计数据
        Map<String, Long> stats = attackStatsService.getAttackStats(limit);

        // 转换为DTO
        List<AttackStatsDTO.AttackRecord> topAttackers = stats.entrySet().stream()
                .map(entry -> new AttackStatsDTO.AttackRecord(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        AttackStatsDTO result = new AttackStatsDTO(
                attackStatsService.getAttackerCount(),
                attackStatsService.getTotalAttackCount(),
                topAttackers
        );

        return ResultVO.success(result);
    }

    /**
     * 查询指定IP的攻击次数
     *
     * @param ip 攻击者IP
     * @return 攻击次数
     */
    @Operation(summary = "查询指定IP攻击次数")
    @ApiMapping(value = "/attack-stats/{ip}", method = RequestMethod.GET)
    public ResultVO<Long> getIpAttackCount(
            @Parameter(description = "攻击者IP")
            @PathVariable String ip,
            HttpServletRequest request) {
        // 鉴权：仅管理员可访问
        userInfoService.checkAdmin(request);
        long count = attackStatsService.getAttackCount(ip);
        return ResultVO.success(count);
    }

    /**
     * 清空攻击统计数据
     *
     * @return 操作结果
     */
    @Operation(summary = "清空攻击统计", description = "清空当前所有攻击统计数据（慎用）")
    @ApiMapping(value = "/attack-stats", method = RequestMethod.DELETE)
    public ResultVO<String> clearAttackStats(
            HttpServletRequest request) {
        // 鉴权：仅管理员可访问
        userInfoService.checkAdmin(request);
        long beforeCount = attackStatsService.getAttackerCount();
        attackStatsService.clearAll();

        log.warn("管理员清空了攻击统计数据，共清除 {} 个IP的记录", beforeCount);
        return ResultVO.success("已清空 " + beforeCount + " 条攻击统计记录");
    }

    /**
     * 移除指定IP的攻击统计
     *
     * @param ip 攻击者IP
     * @return 操作结果
     */
    @Operation(summary = "移除指定IP统计", description = "移除指定IP的攻击统计记录")
    @ApiMapping(value = "/attack-stats/{ip}", method = RequestMethod.DELETE)
    public ResultVO<String> removeIpStats(
            @Parameter(description = "攻击者IP")
            @PathVariable String ip, HttpServletRequest request) {
        // 鉴权：仅管理员可访问
        userInfoService.checkAdmin(request);
        long count = attackStatsService.getAttackCount(ip);
        if (count == 0) {
            return ResultVO.fail("该IP无攻击记录");
        }

        attackStatsService.removeIp(ip);
        log.info("管理员移除了IP {} 的攻击统计，原记录: {} 次", ip, count);
        return ResultVO.success("已移除IP " + ip + " 的攻击统计（原记录: " + count + " 次）");
    }

    /**
     * 获取高频攻击者列表
     *
     * @param threshold 攻击次数阈值，默认20
     * @return 高频攻击者列表
     */
    @Operation(summary = "获取高频攻击者", description = "获取攻击次数超过指定阈值的IP列表")
    @ApiMapping(value = "/high-frequency-attackers", method = RequestMethod.GET)
    public ResultVO<List<AttackStatsDTO.AttackRecord>> getHighFrequencyAttackers(
            @Parameter(description = "攻击次数阈值")
            @RequestParam(defaultValue = "20") Integer threshold
            , HttpServletRequest request) {
        userInfoService.checkAdmin(request);
        Map<String, Long> allStats = attackStatsService.getAttackStats(0);

        List<AttackStatsDTO.AttackRecord> highFrequencyAttackers = allStats.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .map(entry -> new AttackStatsDTO.AttackRecord(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return ResultVO.success(highFrequencyAttackers);
    }
}
