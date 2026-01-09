package cn.tannn.lychnos.entity;

import cn.tannn.lychnos.common.pojo.JpaCommonBean;
import cn.tannn.lychnos.common.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 用户兴趣关联表（关联用户与书籍分析，包含用户反馈）
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 20:00
 */
@Entity
@Table(name = "tb_user_interest",
        indexes = {
                @Index(name = "idx_login_name", columnList = "loginName"),
                @Index(name = "idx_book_analyse_id", columnList = "bookAnalyseId"),
                @Index(name = "idx_user_book", columnList = "loginName,bookAnalyseId", unique = true)
        }
)
@Comment("用户兴趣关联表")
@Getter
@Setter
@ToString
@DynamicUpdate
@DynamicInsert
@Schema(description = "用户兴趣关联表")
@JsonView({Views.Public.class})
public class UserInterest extends JpaCommonBean<UserInterest> {

    /**
     * 用户id, 登录名可能存在更改的问题，id是不是自增的所有是稳定的
     */
    @Column(columnDefinition = " varchar(100) not null ")
    @Comment("用户id")
    @Schema(description = "用户id")
    private String userId;

    /**
     * 书籍分析ID（关联tb_book_analyse表的id）
     */
    @Column(columnDefinition = " bigint not null ")
    @Comment("书籍分析ID")
    @Schema(description = "书籍分析ID")
    private Long bookAnalyseId;

    /**
     * 是否感兴趣
     */
    @Column(columnDefinition = " tinyint(1) ")
    @Comment("是否感兴趣")
    @Schema(description = "是否感兴趣")
    private Boolean interested;

    /**
     * 反馈原因
     */
    @Column(columnDefinition = " text ")
    @Comment("反馈原因")
    @Schema(description = "反馈原因")
    private String reason;

    /**
     * 用户兴趣总结（AI生成的个性化总结）
     */
    @Column(columnDefinition = " text ")
    @Comment("用户兴趣总结")
    @Schema(description = "用户兴趣总结")
    private String interestSummary;

}
