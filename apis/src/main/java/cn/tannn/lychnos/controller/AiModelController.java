package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.dto.AIModelDTO;
import cn.tannn.lychnos.entity.AIModel;
import cn.tannn.lychnos.service.AIModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * ai模型
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/10 10:31
 */
@PathRestController("/ai")
@Tag(name = "AI模型")
@RequiredArgsConstructor
@Slf4j
public class AiModelController {

    private final AIModelService aiModelService;

    @GetMapping("/models")
    @Operation(summary = "获取模型列表")
    public ResultVO<List<AIModel>> list(@RequestParam ModelType type, HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);
        List<AIModel> models = aiModelService.findByUserIdAndType(userId, type);
        return ResultVO.success(models);
    }

    @PostMapping("/models")
    @Operation(summary = "添加模型")
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<AIModel> add(@Valid @RequestBody AIModelDTO dto, HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);
        AIModel model = dto.toEntity(userId);
        aiModelService.saveOne(model);
        return ResultVO.success(model);
    }

    @PutMapping("/models/{id}")
    @Operation(summary = "更新模型")
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<AIModel> update(@PathVariable Long id,
                                    @Valid @RequestBody AIModelDTO dto,
                                    HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);
        AIModel model = aiModelService.findVerifyRole(id,userId);
        dto.updateEntity(model);
        aiModelService.saveOne(model);
        return ResultVO.success(model);
    }

    @DeleteMapping("/models/{id}")
    @Operation(summary = "删除模型")
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);
        AIModel model = aiModelService.findVerifyRole(id,userId);
        aiModelService.getJpaBasicsDao().delete(model);
        return ResultVO.success();
    }

    @PutMapping("/models/{id}/active")
    @Operation(summary = "设置激活的模型")
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Void> setActive(@PathVariable Long id, HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);
        AIModel model = aiModelService.findVerifyRole(id,userId);
        // 先将同类型的所有模型设置为未激活
        aiModelService.disableAllByUserIdAndType(userId, model.getType());
        // 设置当前模型为激活
        model.setEnabled(true);
        aiModelService.saveOne(model);
        return ResultVO.success();
    }
}
