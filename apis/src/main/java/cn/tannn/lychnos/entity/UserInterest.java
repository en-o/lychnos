package cn.tannn.lychnos.entity;

import cn.tannn.jdevelops.jpa.generator.UuidCustomGenerator;
import cn.tannn.lychnos.common.views.Views;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 用户跟书籍兴趣关联（关联用户与书籍分析，包含用户反馈）
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 20:00
 */
@Entity
@Table(name = "tb_user_interest",
        indexes = {
                @Index(name = "idx_user", columnList = "userId"),
                @Index(name = "idx_book_analyse_id", columnList = "bookAnalyseId"),
                @Index(name = "idx_user_book", columnList = "userId,bookAnalyseId", unique = true)
        }
)
@Comment("用户兴趣关联表")
@Getter
@Setter
@ToString
@DynamicUpdate
@DynamicInsert
@Schema(description = "用户跟书籍兴趣关联")
@JsonView({Views.Public.class})
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuidCustomGenerator")
    @GenericGenerator(name = "uuidCustomGenerator", type = UuidCustomGenerator.class)
    @Column(columnDefinition = "bigint")
    @Comment("uuid")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;


    /**
     * 用户id, 登录名可能存在更改的问题，id是不是自增的所有是稳定的
     */
    @Column(columnDefinition = " varchar(100) not null ")
    @Comment("用户id")
    @Schema(description = "用户id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 书籍分析ID（关联tb_book_analyse表的id）
     */
    @Column(columnDefinition = " bigint not null ")
    @Comment("书籍分析ID")
    @Schema(description = "书籍分析ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bookAnalyseId;

    /**
     * 书名（冗余字段，用于检索）
     */
    @Column(columnDefinition = " varchar(255) ")
    @Comment("书名")
    @Schema(description = "书名")
    private String bookTitle;

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

    @CreatedDate
    @Column(columnDefinition = "timestamp", updatable = false)
    @Comment("创建日期")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime createTime;

}
