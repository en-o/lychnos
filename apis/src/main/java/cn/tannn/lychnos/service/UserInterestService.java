package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.jpa.result.JpaPageResult;
import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.jdevelops.util.jpa.select.EnhanceSpecification;
import cn.tannn.lychnos.controller.dto.AnalysisHistoryPage;
import cn.tannn.lychnos.controller.dto.UserInterestFeedback;
import cn.tannn.lychnos.controller.vo.AnalysisHistoryVO;
import cn.tannn.lychnos.controller.vo.UserPreferenceVO;
import cn.tannn.lychnos.dao.UserInterestDao;
import cn.tannn.lychnos.entity.BookAnalyse;
import cn.tannn.lychnos.entity.UserInterest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户兴趣
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 22:07
 */
@Service
@Slf4j
public class UserInterestService extends J2ServiceImpl<UserInterestDao, UserInterest, Long> {

    private final BookAnalyseService bookAnalyseService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserInterestService(BookAnalyseService bookAnalyseService) {
        super(UserInterest.class);
        this.bookAnalyseService = bookAnalyseService;
    }


    /**
     * 创建用户兴趣
     * @param interest  UserInterestFeedback
     */
    public void feedback(@Valid UserInterestFeedback interest, Long userId, BookAnalyse bookAnalyse) {
        UserInterest userInterest = new UserInterest();
        userInterest.setUserId(userId);
        userInterest.setBookAnalyseId(interest.getBookAnalyseId());
        userInterest.setBookTitle(bookAnalyse.getTitle());
        userInterest.setAuthor(bookAnalyse.getAuthor());
        userInterest.setThemes(bookAnalyse.getThemes());
        userInterest.setInterested(interest.getInterested());
        userInterest.setReason(interest.getReason());
        // 这个数据需要异步写入，因为ai可能很慢
        userInterest.setInterestSummary("ai还没准备好");
        userInterest.setCreateTime(LocalDateTime.now());
        getJpaBasicsDao().save(userInterest);
    }

    /**
     * 获取最近分析（返回完整分析历史）
     * @param userId 用户ID
     * @param top 最近多少
     * @return List<AnalysisHistoryVO>
     */
    public List<AnalysisHistoryVO> recentAnalysis(Long userId, int top) {
        // 按创建时间倒序查询
        PageRequest pageRequest = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "createTime"));
        List<UserInterest> interests = getJpaBasicsDao().findAll(
                (root, query, cb) -> cb.equal(root.get("userId"), userId),
                pageRequest
        ).getContent();

        return convertToVOList(interests);
    }

    /**
     * 获取分析历史（分页）
     * @param userId 用户ID
     * @param page 分页查询参数
     * @return JpaPageResult<AnalysisHistoryVO>
     */
    public JpaPageResult<AnalysisHistoryVO> analysisHistory(Long userId, AnalysisHistoryPage page) {
        Specification<UserInterest> where = EnhanceSpecification.where(s -> {
            s.eq(true, "userId", userId);
            s.like(page.getBookTitle() != null && !page.getBookTitle().trim().isEmpty(),
                   "bookTitle", page.getBookTitle());
        });
        Page<UserInterest> interests = getJpaBasicsDao().findAll(
                where,
                page.getPage().pageable()
        );
        List<AnalysisHistoryVO> voList = convertToVOList(interests.getContent());
        return new JpaPageResult<>(interests.getNumber()+1,
                interests.getSize(),
                interests.getTotalPages(),
                interests.getTotalElements(),
                voList);
    }

    /**
     * 批量转换为 VO（列表页懒加载，不查询BookAnalyse）
     * 列表页只显示基本信息，详情页才查询完整的BookAnalyse数据
     */
    private List<AnalysisHistoryVO> convertToVOList(List<UserInterest> interests) {
        return interests.stream().map(interest -> {
            AnalysisHistoryVO vo = new AnalysisHistoryVO();
            vo.setId(interest.getId());
            vo.setTitle(interest.getBookTitle());
            vo.setAuthor(interest.getAuthor());
            vo.setInterested(interest.getInterested());
            vo.setCreateTime(interest.getCreateTime().format(FORMATTER));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 检查用户是否已经分析过该书籍
     * @param userId 用户ID
     * @param bookTitle 书名
     * @return 用户兴趣（如果已分析）
     */
    public Optional<UserInterest> checkAnalyzed(Long userId, String bookTitle) {
        return getJpaBasicsDao().findByUserIdAndBookTitle(userId, bookTitle);
    }

    /**
     * 获取用户偏好分析
     * @param userId 用户ID
     * @return 用户偏好分析
     */
    public UserPreferenceVO getUserPreference(Long userId) {
        // TODO: 后期从统计表中获取数据，当前返回模拟数据
        UserPreferenceVO vo = new UserPreferenceVO();

        // 模拟阅读报告
        UserPreferenceVO.ReadingReport readingReport = new UserPreferenceVO.ReadingReport();
        readingReport.setTotalBooks(15);
        readingReport.setInterestedBooks(12);
        readingReport.setFavoriteGenres(Arrays.asList("科幻", "治愈系", "历史/科普"));
        readingReport.setFavoriteThemes(Arrays.asList("宇宙文明", "温情", "人类演化", "生命意义", "时空穿越"));
        readingReport.setReadingTrend("持续增长");
        vo.setReadingReport(readingReport);

        // 模拟总结
        vo.setSummary("你是一位科幻阅读爱好者，已分析 15 本书，兴趣率达 80%。你特别关注宇宙文明、温情、人类演化等主题。");

        // 模拟年度报告
        int currentYear = LocalDateTime.now().getYear();
        UserPreferenceVO.AnnualReport annualReport = new UserPreferenceVO.AnnualReport();
        annualReport.setYear(currentYear);
        annualReport.setTotalBooks(15);
        annualReport.setInterestedCount(12);

        // 模拟热门类型
        List<UserPreferenceVO.GenreCount> topGenres = new ArrayList<>();
        topGenres.add(createGenreCount("科幻", 6));
        topGenres.add(createGenreCount("治愈系", 4));
        topGenres.add(createGenreCount("历史/科普", 3));
        topGenres.add(createGenreCount("现实主义", 2));
        annualReport.setTopGenres(topGenres);

        // 模拟热门主题
        List<UserPreferenceVO.ThemeCount> topThemes = new ArrayList<>();
        topThemes.add(createThemeCount("宇宙文明", 5));
        topThemes.add(createThemeCount("温情", 4));
        topThemes.add(createThemeCount("人类演化", 3));
        topThemes.add(createThemeCount("生命意义", 3));
        topThemes.add(createThemeCount("时空穿越", 2));
        topThemes.add(createThemeCount("科技哲学", 2));
        topThemes.add(createThemeCount("人生抉择", 2));
        topThemes.add(createThemeCount("认知革命", 2));
        topThemes.add(createThemeCount("救赎", 1));
        topThemes.add(createThemeCount("苦难", 1));
        annualReport.setTopThemes(topThemes);

        // 模拟月度趋势
        List<UserPreferenceVO.MonthlyTrend> monthlyTrend = new ArrayList<>();
        int[] monthlyCounts = {2, 1, 2, 0, 1, 3, 0, 0, 0, 0, 0, 0};
        for (int i = 1; i <= 12; i++) {
            monthlyTrend.add(createMonthlyTrend(i, monthlyCounts[i - 1]));
        }
        annualReport.setMonthlyTrend(monthlyTrend);

        // 模拟亮点
        List<String> highlights = new ArrayList<>();
        highlights.add(String.format("%d年，你探索了 15 本书", currentYear));
        highlights.add("最喜欢的类型：科幻");
        highlights.add("最关注的主题：宇宙文明、温情、人类演化");
        annualReport.setHighlights(highlights);

        vo.setAnnualReport(annualReport);

        return vo;
    }

    private UserPreferenceVO.GenreCount createGenreCount(String genre, Integer count) {
        UserPreferenceVO.GenreCount gc = new UserPreferenceVO.GenreCount();
        gc.setGenre(genre);
        gc.setCount(count);
        return gc;
    }

    private UserPreferenceVO.ThemeCount createThemeCount(String theme, Integer count) {
        UserPreferenceVO.ThemeCount tc = new UserPreferenceVO.ThemeCount();
        tc.setTheme(theme);
        tc.setCount(count);
        return tc;
    }

    private UserPreferenceVO.MonthlyTrend createMonthlyTrend(Integer month, Integer count) {
        UserPreferenceVO.MonthlyTrend mt = new UserPreferenceVO.MonthlyTrend();
        mt.setMonth(month);
        mt.setCount(count);
        return mt;
    }
}
