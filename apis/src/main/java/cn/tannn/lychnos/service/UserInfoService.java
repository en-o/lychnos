package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.exception.built.UserException;
import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.common.constant.UserStatus;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.dto.LoginPassword;
import cn.tannn.lychnos.controller.dto.PasswordEdit;
import cn.tannn.lychnos.controller.dto.UserInfoFix;
import cn.tannn.lychnos.dao.UserInfoDao;
import cn.tannn.lychnos.entity.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static cn.tannn.jdevelops.utils.jwt.exception.UserCode.USER_EXIST_ERROR;
import static cn.tannn.jdevelops.utils.jwt.exception.UserCode.USER_PASSWORD_ERROR;
import static cn.tannn.lychnos.entity.UserInfo.getMd5Password;

/**
 * 用户
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/8 22:36
 */
@Service
@Slf4j
public class UserInfoService extends J2ServiceImpl<UserInfoDao, UserInfo, Long> {
    public UserInfoService() {
        super(UserInfo.class);
    }


    /**
     * 检查是否是管理员
     */
    public void checkAdmin(HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);
        UserInfo userInfo = getJpaBasicsDao().findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!UserUtil.isAdmin(userInfo.getRoles())) {
            throw new RuntimeException("无权限访问，仅管理员可访问");
        }
    }

    /**
     * 用户登录校验，校验成功返回用户信息
     *
     * @param login login
     * @return Account
     */
    public UserInfo authenticateUser(LoginPassword login) {

        Optional<UserInfo> byLoginName = getJpaBasicsDao().findByLoginName(login.getLoginName());
        if (byLoginName.isEmpty()) {
            throw new UserException(USER_EXIST_ERROR);
        }
        UserInfo authUser = byLoginName.get();
        // 密码为空不允许登录
        if (StringUtils.isBlank(login.getPassword())) {
            throw new UserException(USER_PASSWORD_ERROR);
        }
        // 验证 密码
        authUser.verifyUserPassMd5(login.getLoginName(), login.getPassword());
        return authUser;
    }

    /**
     * 注册用户
     *
     * @param register 注册
     * @return UserInfo
     */
    public UserInfo registerUser(UserInfo register) {
        if (userExist(register.getLoginName())) {
            throw new BusinessException("用户已存在，请重新注册");
        }
        if (userExistForEmail(register.getEmail())) {
            throw new BusinessException("邮件已被使用，请更换邮件地址");
        }
        if (StringUtils.isNotBlank(register.getPassword())) {
            register.setPassword(getMd5Password(register.getLoginName(), register.getPassword()));
        } else {
            throw new UserException("注册用户密码不允许为空");
        }

        // 设置默认角色
        if (register.getRoles() == null) {
            register.setRoles(UserUtil.createDefaultRoles());
        }

        return getJpaBasicsDao().save(register);
    }

    /**
     * 用户是否存在
     *
     * @param loginName 登录名
     * @return true 存在 false 不存在
     */
    public boolean userExist(String loginName) {
        Optional<UserInfo> byLoginName = getJpaBasicsDao().findByLoginName(loginName);
        return byLoginName.isPresent();
    }

    /**
     * EMAIL用户是否存在
     *
     * @param email 邮件地址
     * @return true 存在 false 不存在
     */
    public boolean userExistForEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        Optional<UserInfo> emailRes = getJpaBasicsDao().findByEmail(email);
        return emailRes.isPresent();
    }

    /**
     * 修改密码
     *
     * @param loginName 登录名
     * @param password  新密码
     */
    public void editPassword(String loginName, PasswordEdit password) {
        UserInfo userInfo = getJpaBasicsDao().findByLoginName(loginName)
                .orElseThrow(() -> new UserException("请重新登录后再试"));
        // 验证旧密码
        userInfo.verifyUserPassMd5(loginName, password.getOldPassword());

        // 设置新密码
        String md5Password = getMd5Password(loginName, password.getNewPassword());
        userInfo.setPassword(md5Password);
        getJpaBasicsDao().save(userInfo);
    }

    /**
     * 更新基础信息
     *
     * @param fix AccountFixInfo
     * @return Account
     */
    public UserInfo updateInfo(UserInfoFix fix) {
        UserInfo account = getJpaBasicsDao().findByLoginName(fix.getLoginName())
                .orElseThrow(() -> new BusinessException("请核对用户信息"));
        fix.update(account);
        getJpaBasicsDao().save(account);
        return account;
    }

    /**
     * 根据登录名查询用户
     *
     * @param loginName 登录名
     * @return 用户信息
     */
    public Optional<UserInfo> findByLoginName(String loginName) {
        return getJpaBasicsDao().findByLoginName(loginName);
    }

    /**
     * 切换用户状态（启用/封禁）
     *
     * @param userId 用户ID
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserInfo toggleUserStatus(Long userId) {
        UserInfo userInfo = getJpaBasicsDao().findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (UserUtil.isAdmin(userInfo.getRoles())) {
            throw new BusinessException("不允许修改管理员账户状态");
        }

        Integer currentStatus = userInfo.getStatus();
        if (UserStatus.OFFICIAL.getCode().equals(currentStatus)) {
            userInfo.setStatus(UserStatus.PUBLIC.getCode());
        } else if (UserStatus.PUBLIC.getCode().equals(currentStatus)) {
            userInfo.setStatus(UserStatus.OFFICIAL.getCode());
        } else {
            userInfo.setStatus(UserStatus.OFFICIAL.getCode());
        }

        return getJpaBasicsDao().save(userInfo);
    }

    /**
     * 验证用户状态
     *
     * @param userInfo 用户信息
     */
    public void validateUserStatus(UserInfo userInfo) {
        Integer status = userInfo.getStatus();
        if (UserStatus.PUBLIC.getCode().equals(status)) {
            throw new UserException("账户已被封禁，无法登录");
        }
        if (UserStatus.PRIVATE.getCode().equals(status)) {
            throw new UserException("账户已注销，无法登录");
        }
    }


}
