package cn.tannn.lychnos.controller.admin;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.controller.vo.UserDetailVO;
import cn.tannn.lychnos.entity.UserInfo;
import cn.tannn.lychnos.entity.UserThirdPartyBind;
import cn.tannn.lychnos.service.UserInfoService;
import cn.tannn.lychnos.dao.UserThirdPartyBindDao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
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
     * 获取所有用户列表
     */
    @Operation(summary = "获取所有用户列表")
    @ApiMapping(value = "/list", method = RequestMethod.GET)
    public ResultVO<List<UserDetailVO>> listUsers(HttpServletRequest request) {
        userInfoService.checkAdmin(request);

        List<UserInfo> users = userInfoService.getJpaBasicsDao().findAll();
        List<UserDetailVO> vos = users.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        return ResultVO.success(vos);
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

        return ResultVO.success(convertToDetailVO(userInfo));
    }

    /**
     * 获取用户的第三方绑定列表
     */
    @Operation(summary = "获取用户的第三方绑定列表")
    @ApiMapping(value = "/third-party-bindings/{userId}", method = RequestMethod.GET)
    public ResultVO<List<UserThirdPartyBind>> getUserThirdPartyBindings(@PathVariable Long userId, HttpServletRequest request) {
        userInfoService.checkAdmin(request);
        List<UserThirdPartyBind> bindings = userThirdPartyBindDao.findByUserId(userId);
        return ResultVO.success(bindings);
    }

    /**
     * 转换为用户详情VO
     */
    private UserDetailVO convertToDetailVO(UserInfo userInfo) {
        UserDetailVO vo = new UserDetailVO();
        vo.setId(userInfo.getId());
        vo.setLoginName(userInfo.getLoginName());
        vo.setNickname(userInfo.getNickname());
        vo.setEmail(userInfo.getEmail());
        vo.setRoles(userInfo.getRoles());
        vo.setCreateTime(userInfo.getCreateTime() != null ? userInfo.getCreateTime().toString() : null);
        vo.setUpdateTime(userInfo.getUpdateTime() != null ? userInfo.getUpdateTime().toString() : null);
        return vo;
    }
}
