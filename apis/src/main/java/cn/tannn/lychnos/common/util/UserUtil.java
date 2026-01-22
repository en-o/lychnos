package cn.tannn.lychnos.common.util;

import cn.tannn.jdevelops.jwt.standalone.service.LoginService;
import cn.tannn.jdevelops.jwt.standalone.util.JwtWebUtil;
import cn.tannn.jdevelops.utils.http.IpUtil;
import cn.tannn.jdevelops.utils.jwt.core.JwtService;
import cn.tannn.jdevelops.utils.jwt.module.LoginJwtExtendInfo;
import cn.tannn.jdevelops.utils.jwt.module.SignEntity;
import cn.tannn.lychnos.common.pojo.UserRequestInfo;
import cn.tannn.lychnos.controller.LoginController;
import cn.tannn.lychnos.entity.UserInfo;
import com.alibaba.fastjson2.JSONArray;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户相关
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2024/7/8 下午12:35
 */
@Slf4j
public class UserUtil extends cn.tannn.jdevelops.jwt.standalone.util.UserUtil {
    /**
     * 获得 LoginJwtExtendInfo
     *
     * @param request HttpServletRequest
     * @return LoginJwtExtendInfo
     */
    public static LoginJwtExtendInfo<String> getLoginJwtExtendInfoExpires(HttpServletRequest request) {
        String token = JwtWebUtil.getToken(request);
        return JwtService.getLoginJwtExtendInfoExpires(token);
    }

    /**
     * 获取用户请求信息（从JWT中获取，避免数据库查询）
     */
    public static UserRequestInfo userRequestInfo() {
        try {
            var attributes = RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            var request = ((ServletRequestAttributes) attributes).getRequest();
            var jwtInfo = UserUtil.getLoginJwtExtendInfoExpires(request);
            return new UserRequestInfo(IpUtil.getPoxyIpEnhance(request), jwtInfo.getUserName(), jwtInfo.getLoginName(),
                    jwtInfo.getUserId());
        } catch (Exception e) {
            log.warn("从JWT获取用户信息失败", e);
            return null;
        }
    }

    /**
     * 获得 userId
     *
     * @param request HttpServletRequest
     * @return userId
     */
    public static String userId(HttpServletRequest request) {
        return getLoginJwtExtendInfoExpires(request).getUserId();
    }

    /**
     * 获得 userId
     *
     * @param request HttpServletRequest
     * @return userId
     */
    public static Long userId2(HttpServletRequest request) {
        return Long.valueOf(getLoginJwtExtendInfoExpires(request).getUserId());
    }

    /**
     * 获得 loginName
     * <p>
     * jwt subject = loginName
     * {@link LoginController#loginUserSign(UserInfo, HttpServletRequest)}
     * </p>
     *
     * @param request HttpServletRequest
     * @return loginName
     */
    public static String loginName(HttpServletRequest request) {
        return JwtWebUtil.getTokenSubjectExpires(request);
    }

    /**
     * 生成登录Token
     *
     * @param loginService 登录服务
     * @param userInfo     用户信息
     * @return token
     */
    public static String generateLoginToken(LoginService loginService, UserInfo userInfo) {
        SignEntity<LoginJwtExtendInfo<String>> init = SignEntity.init(userInfo.getLoginName());
        // 拓展信息
        LoginJwtExtendInfo<String> loginJwtExtendInfo = new LoginJwtExtendInfo<>();
        loginJwtExtendInfo.setUserId(userInfo.getId() + "");
        loginJwtExtendInfo.setUserNo(userInfo.getId() + "");
        loginJwtExtendInfo.setUserName(userInfo.getNickname());
        loginJwtExtendInfo.setLoginName(userInfo.getLoginName());
        init.setMap(loginJwtExtendInfo);
        return loginService.login(init).getSign();
    }


    /**
     * 创建默认角色
     *
     * @return 默认角色数组 ["USER"]
     */
    public static JSONArray createDefaultRoles() {
        JSONArray roles = new JSONArray();
        roles.add("USER");
        return roles;
    }

    /**
     * 判断角色列表中是否包含指定角色（忽略大小写）
     *
     * @param roles    角色列表
     * @param roleName 角色名称
     * @return true-包含，false-不包含
     */
    public static boolean hasRole(JSONArray roles, String roleName) {
        if (roles == null || roles.isEmpty() || roleName == null) {
            return false;
        }
        for (Object role : roles) {
            if (role != null && role.toString().equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否是管理员
     *
     * @param roles 角色列表
     * @return true-是管理员，false-不是管理员
     */
    public static boolean isAdmin(JSONArray roles) {
        return hasRole(roles, "ADMIN");
    }
}
