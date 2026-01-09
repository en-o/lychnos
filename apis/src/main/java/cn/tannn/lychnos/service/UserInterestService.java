package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.jpa.result.JpaPageResult;
import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.jdevelops.util.jpa.select.EnhanceSpecification;
import cn.tannn.lychnos.controller.dto.AnalysisHistoryPage;
import cn.tannn.lychnos.controller.dto.UserInterestFeedback;
import cn.tannn.lychnos.controller.vo.AnalysisHistoryVO;
import cn.tannn.lychnos.dao.UserInterestDao;
import cn.tannn.lychnos.entity.BookAnalyse;
import cn.tannn.lychnos.entity.UserInterest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    public void feedback(@Valid UserInterestFeedback interest, Long userId) {
        UserInterest userInterest = new UserInterest();
        userInterest.setUserId(userId);
        userInterest.setBookAnalyseId(interest.getBookAnalyseId());
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

        return interests.stream().map(this::convertToVO).collect(Collectors.toList());
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
        });
        Page<UserInterest> interests = getJpaBasicsDao().findAll(
                where,
                page.getPage().pageable()
        );
        Page<AnalysisHistoryVO> voPage = interests.map(this::convertToVO);
        return JpaPageResult.toPage(voPage);
    }

    /**
     * 转换为 VO
     */
    private AnalysisHistoryVO convertToVO(UserInterest interest) {
        AnalysisHistoryVO vo = new AnalysisHistoryVO();
        vo.setId(interest.getId());
        vo.setInterested(interest.getInterested());
        vo.setCreateTime(interest.getCreateTime().format(FORMATTER));

        // 获取书籍分析数据
        BookAnalyse bookAnalyse = bookAnalyseService.findById(interest.getBookAnalyseId()).orElse(null);
        if (bookAnalyse != null) {
            vo.setTitle(bookAnalyse.getTitle());
            vo.setAnalysisData(bookAnalyse);
        }

        return vo;
    }
}
