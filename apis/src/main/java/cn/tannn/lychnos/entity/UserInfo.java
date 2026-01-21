package cn.tannn.lychnos.entity;

import cn.hutool.crypto.SecureUtil;
import cn.tannn.jdevelops.exception.built.UserException;
import cn.tannn.lychnos.common.pojo.JpaCommonBean;
import cn.tannn.lychnos.common.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.tannn.jdevelops.utils.jwt.exception.UserCode.USER_PASSWORD_ERROR;

/**
 * 用户表
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/8 21:54
 */
@Entity
@Table(name = "tb_user_info", indexes = {
        @Index(name = "idx_loginName", columnList = "loginName", unique = true),
        @Index(name = "idx_email", columnList = "email")
})
@Comment("账户基础信息表")
@Getter
@Setter
@ToString
@DynamicUpdate
@DynamicInsert
@Schema(description = "账户基础信息表")
@JsonView({ Views.Public.class })
public class UserInfo extends JpaCommonBean<UserInfo> {

    /**
     * 登录名/用户名
     */
    @Column(columnDefinition = " varchar(100)  not null ")
    @Comment("登录名/用户名")
    @Schema(description = "登录名/用户名")
    private String loginName;

    /**
     * 登录密码
     */
    @Column(columnDefinition = " varchar(100) not null ")
    @Comment("登录密码")
    @Schema(description = "登录密码")
    @JsonView(Views.UserPassword.class)
    private String password;

    /**
     * 昵称
     */
    @Column(columnDefinition = " varchar(100) ")
    @Comment("昵称")
    @ColumnDefault("'贵宾'")
    @Schema(description = "昵称")
    private String nickname;

    @Column(columnDefinition = " varchar(100) ")
    @Comment("邮箱")
    @Schema(description = "邮箱")
    private String email;

    /**
     * 用户输入跟数据库密码对比
     *
     * @param loginName 用户输入的登录名(不使用内置的处理数据库忽略大小写的问题)
     * @param inputPass 用户输入密码
     */
    public void verifyUserPassMd5(String loginName, String inputPass) {
        String loginPwdLocal = getMd5Password(loginName, inputPass.trim());
        if (!StringUtils.equals(loginPwdLocal, password)) {
            throw new UserException(USER_PASSWORD_ERROR);
        }
    }

    /**
     * 获取md5密码（用户密码）
     *
     * @param loginName       用户名
     * @param settingPassword 需要设置的密码
     * @return String
     */
    public static String getMd5Password(String loginName, String settingPassword) {
        return SecureUtil.md5(settingPassword.trim() + loginName.trim());
    }
}
