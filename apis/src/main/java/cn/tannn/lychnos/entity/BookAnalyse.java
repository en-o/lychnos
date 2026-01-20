package cn.tannn.lychnos.entity;

import cn.tannn.lychnos.common.pojo.JpaCommonBean;
import com.alibaba.fastjson2.JSONArray;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 书籍分析（平台共享数据，存储书籍的AI分析结果）
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 14:10
 */
@Entity
@Table(name = "tb_book_analyse",
        indexes = {
                @Index(name = "idx_title", columnList = "title", unique = true)
        }
)
@Comment("书籍分析")
@Getter
@Setter
@ToString
@DynamicUpdate
@DynamicInsert
@Schema(description = "书籍分析")
public class BookAnalyse extends JpaCommonBean<BookAnalyse> {

    /**
     * 书名
     */
    @Column(columnDefinition = " varchar(500) not null ")
    @Comment("书名")
    @Schema(description = "书名")
    private String title;

    /**
     * 作者
     */
    @Column(columnDefinition = " varchar(200) ")
    @Comment("作者")
    @Schema(description = "作者")
    private String author;

    /**
     * 类型/流派
     */
    @Column(columnDefinition = " varchar(200) ")
    @Comment("类型/流派")
    @Schema(description = "类型/流派")
    private String genre;

    /**
     * 主题（JSON数组字符串）
     */
    @Column(columnDefinition = " json ")
    @Comment("主题")
    @Schema(description = "主题")
    @JdbcTypeCode(SqlTypes.JSON)
    private JSONArray themes;

    /**
     * 基调
     */
    @Column(columnDefinition = " varchar(200) ")
    @Comment("基调")
    @Schema(description = "基调")
    private String tone;

    /**
     * 关键要素（JSON数组字符串）
     */
    @Column(columnDefinition = " json ")
    @Comment("关键要素")
    @Schema(description = "关键要素")
    @JdbcTypeCode(SqlTypes.JSON)
    private JSONArray keyElements;


    /**
     * 分析图URL
     */
    @Column(columnDefinition = " varchar(500) ")
    @Comment("分析图URL")
    @Schema(description = "分析图URL")
    private String posterUrl;

    /**
     * AI生成的book综述
     */
    @Column(columnDefinition = " text ")
    @Comment("book综述")
    @Schema(description = "book综述")
    private String recommendation;

}
