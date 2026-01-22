package cn.tannn.lychnos.controller.admin;

import cn.tannn.jdevelops.annotations.jdbctemplate.sql.enums.NullHandleStrategy;
import cn.tannn.jdevelops.annotations.jdbctemplate.sql.enums.ParameterMode;
import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.jdectemplate.sql.DynamicSqlBuilder;
import cn.tannn.jdevelops.jpa.result.JpaPageResult;
import cn.tannn.jdevelops.result.response.PageResult;
import cn.tannn.jdevelops.result.response.ResultPageVO;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.constant.ShareType;
import cn.tannn.lychnos.controller.dto.AIModelPageDTO;
import cn.tannn.lychnos.controller.vo.AIModelWithUserVO;
import cn.tannn.lychnos.entity.AIModel;
import cn.tannn.lychnos.service.AIModelService;
import cn.tannn.lychnos.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * 管理员-AI模型管理
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/22
 */
@PathRestController("sys-manage/ai-model")
@Tag(name = "系统管理-AI模型管理")
@RequiredArgsConstructor
@Slf4j
public class AdminAIModelController {

    private final AIModelService aiModelService;
    private final UserInfoService userInfoService;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 分页查询AI模型列表
     */
    @Operation(summary = "分页查询AI模型列表")
    @ApiMapping(value = "/list", method = RequestMethod.POST)
    public ResultPageVO<AIModelWithUserVO, PageResult<AIModelWithUserVO>> listModels(@RequestBody AIModelPageDTO dto, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        DynamicSqlBuilder builder = buildAIModelListSql(dto);

        List<AIModelWithUserVO> voList = namedParameterJdbcTemplate.query(
                builder.getSql(),
                builder.getNamedParams(),
                new DataClassRowMapper<>(AIModelWithUserVO.class)
        );
        // 获取总记录数
        Long total = namedParameterJdbcTemplate.queryForObject(
                builder.buildCountSql().getSql(),
                builder.getNamedParams(),
                Long.class
        );
        // 构建分页结果
        PageResult<AIModelWithUserVO> result = JpaPageResult.page(dto.getPage(), total==null?0L:total, voList);
        return ResultPageVO.success(result);
    }

    /**
     * 构建AI模型列表查询SQL
     */
    private DynamicSqlBuilder buildAIModelListSql(AIModelPageDTO dto) {
        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                "SELECT a.id, a.user_id as userId, u.login_name as loginName, a.name, a.model, a.factory, " +
                "a.enabled, a.type, a.share, a.create_time as createTime, a.update_time as updateTime " +
                "FROM tb_ai_model a LEFT JOIN tb_user_info u ON a.user_id = u.id ",
                ParameterMode.NAMED
        );

        builder.dynamicLike("u.login_name", dto.getLoginName(), NullHandleStrategy.IGNORE)
                .dynamicLike("a.model", dto.getModel(), NullHandleStrategy.IGNORE)
                .dynamicEq("u.nickname", dto.getNickname(), NullHandleStrategy.IGNORE)
                .orderBy("a.create_time DESC")
                .pageZero(dto.getPage().getPageIndex(), dto.getPage().getPageSize());

        return builder;
    }

    /**
     * 设置模型为官方
     */
    @Operation(summary = "设置模型为官方")
    @ApiMapping(value = "/set-official/{id}", method = RequestMethod.PUT)
    public ResultVO<Void> setOfficial(@PathVariable Long id, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        AIModel model = aiModelService.getJpaBasicsDao().findById(id)
                .orElseThrow(() -> new RuntimeException("模型不存在"));

        model.setShare(ShareType.OFFICIAL.getCode());
        aiModelService.getJpaBasicsDao().save(model);

        log.info("管理员设置模型为官方：{} (ID: {})", model.getName(), id);
        return ResultVO.successMessage("已设置为官方模型");
    }

    /**
     * 设置模型为私人
     */
    @Operation(summary = "设置模型为私人")
    @ApiMapping(value = "/set-private/{id}", method = RequestMethod.PUT)
    public ResultVO<Void> setPrivate(@PathVariable Long id, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        AIModel model = aiModelService.getJpaBasicsDao().findById(id)
                .orElseThrow(() -> new RuntimeException("模型不存在"));

        model.setShare(ShareType.PRIVATE.getCode());
        aiModelService.getJpaBasicsDao().save(model);

        log.info("管理员设置模型为私人：{} (ID: {})", model.getName(), id);
        return ResultVO.successMessage("已设置为私人模型");
    }

}
