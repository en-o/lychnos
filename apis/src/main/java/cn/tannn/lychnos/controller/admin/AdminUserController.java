package cn.tannn.lychnos.controller.admin;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.jpa.result.JpaPageResult;
import cn.tannn.jdevelops.result.response.ResultPageVO;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.views.Views;
import cn.tannn.lychnos.controller.dto.UserPageDTO;
import cn.tannn.lychnos.controller.vo.UserDetailVO;
import cn.tannn.lychnos.dao.UserInfoDao;
import cn.tannn.lychnos.entity.UserInfo;
import cn.tannn.lychnos.entity.UserThirdPartyBind;
import cn.tannn.lychnos.service.UserInfoService;
import cn.tannn.lychnos.dao.UserThirdPartyBindDao;
import com.fasterxml.jackson.annotation.JsonView;
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
import java.util.stream.Collectors;

/**
 * 管理员-用户管理
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/22
 */
@PathRestController("sys-manage/user")
@Tag(name = "系统管理-用户管理")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserInfoService userInfoService;
    private final UserThirdPartyBindDao userThirdPartyBindDao;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表")
    @ApiMapping(value = "/list", method = RequestMethod.POST)
    public ResultPageVO<UserDetailVO, JpaPageResult<UserDetailVO>> listUsers(@RequestBody UserPageDTO dto, HttpServletRequest request) {
        userInfoService.checkAdmin(request);
        Page<UserInfo> userPage = userInfoService.findPage(dto, dto.getPage());
        JpaPageResult<UserDetailVO> pageResult = JpaPageResult.toPage(userPage, UserDetailVO.class);
        return ResultPageVO.success(pageResult);
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情")
    @ApiMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public ResultVO<UserDetailVO> getUserDetail(@PathVariable Long id, HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        UserInfo userInfo = userInfoService.getJpaBasicsDao().findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        UserDetailVO userDetailVO = userInfo.to(UserDetailVO.class);
        return ResultVO.success(userDetailVO);
    }

    /**
     * 获取用户的第三方绑定列表
     */
    @Operation(summary = "获取用户的第三方绑定列表")
    @ApiMapping(value = "/third-party-bindings/{userId}", method = RequestMethod.GET)
    @JsonView(Views.ThirdPartyBindAdmin.class)
    public ResultVO<List<UserThirdPartyBind>> getUserThirdPartyBindings(@PathVariable Long userId, HttpServletRequest request) {
        userInfoService.checkAdmin(request);
        List<UserThirdPartyBind> bindings = userThirdPartyBindDao.findByUserId(userId);
        return ResultVO.success(bindings);
    }

}
