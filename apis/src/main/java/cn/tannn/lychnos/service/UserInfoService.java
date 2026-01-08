package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.exception.built.UserException;
import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.controller.dto.LoginPassword;
import cn.tannn.lychnos.dao.UserInfoDao;
import cn.tannn.lychnos.entity.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
public class UserInfoService extends J2ServiceImpl<UserInfoDao, UserInfo,Long> {
    public UserInfoService() {
        super(UserInfo.class);
    }


    /**
     * 用户登录校验，校验成功返回用户信息
     *
     * @param login     login
     * @return Account
     */
    UserInfo authenticateUser(LoginPassword login){

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
     * @param register 注册
     * @return UserInfo
     */
    public UserInfo registerUser(UserInfo register) {
        if (userExist(register.getLoginName())) {
            throw new BusinessException("用户已存在，请重新注册");
        }
        if (StringUtils.isNotBlank(register.getPassword())) {
            register.setPassword(getMd5Password(register.getLoginName(), register.getPassword()));
        } else {
            throw new UserException("注册用户密码不允许为空");
        }
        return getJpaBasicsDao().save(register);
    }


    /**
     * 用户是否存在
     * @param loginName 登录名
     * @return true 存在 false 不存在
     */
    public boolean userExist(String loginName) {
        Optional<UserInfo> byLoginName = getJpaBasicsDao().findByLoginName(loginName);
        return byLoginName.isPresent();
    }

    /**
     * 修改密码
     * @param loginName   登录名
     * @param newPassword 新密码
     */
    public void editPassword(String loginName, String newPassword) {
        Optional<UserInfo> byLoginName = getJpaBasicsDao().findByLoginName(loginName);
        byLoginName.ifPresent(entity -> {
            String md5Password = getMd5Password(loginName, newPassword);
            entity.setPassword(md5Password);
            getJpaBasicsDao().save(entity);
        });
    }
}
