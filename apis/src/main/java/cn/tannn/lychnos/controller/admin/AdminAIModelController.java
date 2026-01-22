package cn.tannn.lychnos.controller.admin;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.jpa.result.JpaPageResult;
import cn.tannn.jdevelops.result.response.ResultPageVO;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.constant.ShareType;
import cn.tannn.lychnos.controller.dto.AIModelPageDTO;
import cn.tannn.lychnos.controller.vo.AIModelWithUserVO;
import cn.tannn.lychnos.dao.AIModelDao;
import cn.tannn.lychnos.dao.UserInfoDao;
import cn.tannn.lychnos.entity.AIModel;
import cn.tannn.lychnos.entity.UserInfo;
import cn.tannn.lychnos.service.AIModelService;
import cn.tannn.lychnos.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final AIModelDao aiModelDao;
    private final UserInfoDao userInfoDao;

    /**
     * 分页查询AI模型列表
     */
    @Operation(summary = "分页查询AI模型列表")
    @ApiMapping(value = "/list", method = RequestMethod.POST)
    public ResultPageVO<AIModelWithUserVO, JpaPageResult<AIModelWithUserVO>> listModels(@RequestBody AIModelPageDTO dto, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        Pageable pageable = dto.getPage().pageable();

        // 构建查询条件
        Specification<AIModel> spec = (root, query, cb) -> {
            if (StringUtils.isNotBlank(dto.getLoginName())) {
                // 根据用户名查询用户ID列表
                List<Long> userIds = userInfoDao.findAll().stream()
                        .filter(u -> u.getLoginName().contains(dto.getLoginName()))
                        .map(UserInfo::getId)
                        .toList();

                if (userIds.isEmpty()) {
                    return cb.disjunction(); // 没有匹配的用户，返回空结果
                }
                List<Long> limitUser = userIds.stream().limit(10).toList();
                return root.get("userId").in(limitUser);
            }
            return cb.conjunction();
        };

        Page<AIModel> modelPage = aiModelDao.findAll(spec, pageable);

        // 获取所有用户信息
        List<Long> userIds = modelPage.getContent().stream()
                .map(AIModel::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> userMap = userInfoDao.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserInfo::getId, UserInfo::getLoginName));

        // 转换为VO
        List<AIModelWithUserVO> voList = modelPage.getContent().stream()
                .map(model -> convertToVO(model, userMap.get(model.getUserId())))
                .collect(Collectors.toList());

        JpaPageResult<AIModelWithUserVO> result = new JpaPageResult<>(
                modelPage.getNumber() + 1,
                modelPage.getSize(),
                modelPage.getTotalPages(),
                modelPage.getTotalElements(),
                voList
        );

        return ResultPageVO.success(result);
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

    /**
     * 转换为VO
     */
    private AIModelWithUserVO convertToVO(AIModel model, String loginName) {
        AIModelWithUserVO vo = new AIModelWithUserVO();
        vo.setId(model.getId());
        vo.setUserId(model.getUserId());
        vo.setLoginName(loginName);
        vo.setName(model.getName());
        vo.setModel(model.getModel());
        vo.setFactory(model.getFactory());
        vo.setEnabled(model.getEnabled());
        vo.setType(model.getType() != null ? model.getType().name() : null);
        vo.setShare(model.getShare());
        vo.setCreateTime(model.getCreateTime() != null ? model.getCreateTime().toString() : null);
        vo.setUpdateTime(model.getUpdateTime() != null ? model.getUpdateTime().toString() : null);
        return vo;
    }
}
