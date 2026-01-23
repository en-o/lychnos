package cn.tannn.lychnos.controller.admin;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.controller.dto.UserAnalysisLogQueryDTO;
import cn.tannn.lychnos.controller.vo.UserAnalysisLogVO;
import cn.tannn.lychnos.service.UserAnalysisLogService;
import cn.tannn.lychnos.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

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
}
