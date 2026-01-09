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
 * 书籍分析
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 14:10
 */
@Entity
@Table(name = "tb_book_analys",
        indexes = {
                @Index(name = "idx_loginName", columnList = "loginName", unique = true)
        }
)
@Comment("书籍分析")
@Getter
@Setter
@ToString
@DynamicUpdate
@DynamicInsert
@Schema(description = "书籍分析")
@JsonView({Views.Public.class})
public class BookAnalyse extends JpaCommonBean<BookAnalyse> {


}
