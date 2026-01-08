package cn.tannn.lychnos.entity;

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
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 用户表
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/8 21:54
 */
@Entity
@Table(name = "tb_user_info",
        indexes = {
                @Index(name = "idx_loginName", columnList = "loginName", unique = true)
        }
)
@Comment("账户基础信息表")
@Getter
@Setter
@ToString
@DynamicUpdate
@DynamicInsert
@Schema(description = "账户基础信息表")
@JsonView({Views.Public.class})
public class UserInfo extends JpaCommonBean<UserInfo> {

    /**
     * 登录名
     */
    @Column(columnDefinition = " varchar(100)  not null ")
    @Comment("登录名")
    @Schema(description = "登录名")
    private String loginName;

    /**
     * 登录密码
     */
    @Column(columnDefinition = " varchar(100) not null ")
    @Comment("登录密码")
    @Schema(description = "登录密码")
    @JsonView(Views.UserPassword.class)
    private String password;
}
