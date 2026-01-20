package cn.tannn.lychnos.common.util;


import cn.tannn.jdevelops.jwt.standalone.util.JwtWebUtil;
import cn.tannn.jdevelops.utils.http.IpUtil;
import cn.tannn.jdevelops.utils.jwt.core.JwtService;
import cn.tannn.jdevelops.utils.jwt.module.LoginJwtExtendInfo;
import cn.tannn.lychnos.common.pojo.UserRequestInfo;
import cn.tannn.lychnos.controller.LoginController;
import cn.tannn.lychnos.entity.UserInfo;
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
            return new UserRequestInfo(IpUtil.getPoxyIpEnhance(request)
                    ,jwtInfo.getUserName()
                    ,jwtInfo.getLoginName()
                    ,jwtInfo.getUserId());
        } catch (Exception e) {
            log.warn("从JWT获取用户信息失败", e);
            return null;
        }
    }


    /**
     * 获得 userId
     * @param request HttpServletRequest
     * @return userId
     */
    public static String userId(HttpServletRequest request) {
        return getLoginJwtExtendInfoExpires(request).getUserId();
    }


    /**
     * 获得 userId
     * @param request HttpServletRequest
     * @return userId
     */
    public static Long userId2(HttpServletRequest request) {
        return Long.valueOf(getLoginJwtExtendInfoExpires(request).getUserId());
    }

    /**
     * 获得 loginName
     * <p> jwt subject = loginName   {@link LoginController#loginUserSign(UserInfo, HttpServletRequest)}</p>
     * @param request HttpServletRequest
     * @return loginName
     */
    public static String loginName(HttpServletRequest request) {
        return JwtWebUtil.getTokenSubjectExpires(request);
    }



}
