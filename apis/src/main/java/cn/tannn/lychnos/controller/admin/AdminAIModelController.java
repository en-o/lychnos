package cn.tannn.lychnos.controller.admin;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.constant.ShareType;
import cn.tannn.lychnos.entity.AIModel;
import cn.tannn.lychnos.service.AIModelService;
import cn.tannn.lychnos.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * 获取所有AI模型列表
     */
    @Operation(summary = "获取所有AI模型列表")
    @ApiMapping(value = "/list", method = RequestMethod.GET)
    public ResultVO<List<AIModel>> listModels(HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        List<AIModel> models = aiModelService.getJpaBasicsDao().findAll();
        return ResultVO.success(models);
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
