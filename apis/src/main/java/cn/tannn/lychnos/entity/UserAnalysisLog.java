package cn.tannn.lychnos.entity;

import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.common.constant.ShareType;
import cn.tannn.lychnos.common.constant.UsageType;
import cn.tannn.lychnos.common.pojo.JpaCommonBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 用户分析日志
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
@Entity
@Table(name = "tb_user_analysis_log",
        indexes = {
                @Index(name = "idx_user_id", columnList = "userId"),
                @Index(name = "idx_usage_type", columnList = "usageType"),
                @Index(name = "idx_create_time", columnList = "createTime"),
                @Index(name = "idx_book_analyse_id", columnList = "bookAnalyseId")
        }
)
@Comment("用户分析日志")
@Getter
@Setter
@ToString
@DynamicUpdate
@DynamicInsert
@Schema(description = "用户分析日志")
public class UserAnalysisLog extends JpaCommonBean<UserAnalysisLog> {

    @Column(columnDefinition = "bigint not null")
    @Comment("用户ID")
    @Schema(description = "用户ID")
    private Long userId;

    @Column(columnDefinition = "varchar(100)")
    @Comment("用户姓名")
    @Schema(description = "用户姓名")
    private String userName;

    @Column(columnDefinition = "varchar(50)")
    @Comment("调用IP")
    @Schema(description = "调用IP")
    private String callIp;

    @Column(columnDefinition = "bigint")
    @Comment("模型ID")
    @Schema(description = "模型ID")
    private Long modelId;

    @Column(columnDefinition = "varchar(200)")
    @Comment("模型名称")
    @Schema(description = "模型名称")
    private String modelName;

    @Column(columnDefinition = "varchar(100)")
    @Comment("模型厂商")
    @Schema(description = "模型厂商")
    private String modelVendor;

    @Column(columnDefinition = "varchar(20)")
    @Comment("模型类型：TEXT/IMAGE")
    @Schema(description = "模型类型")
    @Enumerated(EnumType.STRING)
    private ModelType modelType;

    @Column(columnDefinition = "int")
    @Comment("模型来源：0-官方/1-私人/2-公开")
    @Schema(description = "模型来源")
    private Integer modelSource;

    @Column(columnDefinition = "varchar(20) not null")
    @Comment("用途：BOOK_PARSE/BOOK_IMAGE")
    @Schema(description = "用途")
    @Enumerated(EnumType.STRING)
    private UsageType usageType;

    @Column(columnDefinition = "varchar(500)")
    @Comment("书籍标题")
    @Schema(description = "书籍标题")
    private String bookTitle;

    @Column(columnDefinition = "bigint")
    @Comment("书籍分析ID")
    @Schema(description = "书籍分析ID")
    private Long bookAnalyseId;

    @Column(columnDefinition = "boolean")
    @Comment("是否成功")
    @Schema(description = "是否成功")
    @ColumnDefault("true")
    private Boolean success;

    @Column(columnDefinition = "text")
    @Comment("错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    @Column(columnDefinition = "boolean")
    @Comment("是否直接使用已有数据")
    @Schema(description = "是否直接使用已有数据")
    @ColumnDefault("false")
    private Boolean useExistingData;
}
