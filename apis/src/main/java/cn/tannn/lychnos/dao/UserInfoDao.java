package cn.tannn.lychnos.dao;

import cn.tannn.jdevelops.jpa.repository.JpaBasicsRepository;
import cn.tannn.lychnos.entity.UserInfo;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

/**
 * 用户
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/8 22:35
 */
public interface UserInfoDao extends JpaBasicsRepository<UserInfo,Long> {

    /**
     * 根据登录名查询用户
     * @param loginName 登录名
     * @return 用户信息
     */
    Optional<UserInfo> findByLoginName(@NotBlank String loginName);


    /**
     * 根据登录名邮件用户 - 一个邮件只能绑定一个人
     * @param email 邮件地址
     * @return 用户信息
     */
    Optional<UserInfo> findByEmail(@NotBlank String email);
}
