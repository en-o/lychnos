package cn.tannn.lychnos.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 用户偏好分析VO
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/10
 */
@Schema(description = "用户偏好分析")
@ToString
@Getter
@Setter
public class UserPreferenceVO {

    @Schema(description = "偏好总结")
    private String summary;

    @Schema(description = "阅读报告")
    private ReadingReport readingReport;

    @Schema(description = "年度报告")
    private AnnualReport annualReport;

    @Getter
    @Setter
    public static class ReadingReport {
        @Schema(description = "总书籍数")
        private Integer totalBooks;

        @Schema(description = "感兴趣书籍数")
        private Integer interestedBooks;

        @Schema(description = "喜欢的类型")
        private List<String> favoriteGenres;

        @Schema(description = "喜欢的主题")
        private List<String> favoriteThemes;

        @Schema(description = "阅读趋势")
        private String readingTrend;
    }

    @Getter
    @Setter
    public static class AnnualReport {
        @Schema(description = "年份")
        private Integer year;

        @Schema(description = "总书籍数")
        private Integer totalBooks;

        @Schema(description = "感兴趣数量")
        private Integer interestedCount;

        @Schema(description = "热门类型")
        private List<GenreCount> topGenres;

        @Schema(description = "热门主题")
        private List<ThemeCount> topThemes;

        @Schema(description = "月度趋势")
        private List<MonthlyTrend> monthlyTrend;

        @Schema(description = "亮点")
        private List<String> highlights;
    }

    @Getter
    @Setter
    public static class GenreCount {
        @Schema(description = "类型")
        private String genre;

        @Schema(description = "数量")
        private Integer count;
    }

    @Getter
    @Setter
    public static class ThemeCount {
        @Schema(description = "主题")
        private String theme;

        @Schema(description = "数量")
        private Integer count;
    }

    @Getter
    @Setter
    public static class MonthlyTrend {
        @Schema(description = "月份")
        private Integer month;

        @Schema(description = "数量")
        private Integer count;
    }
}
