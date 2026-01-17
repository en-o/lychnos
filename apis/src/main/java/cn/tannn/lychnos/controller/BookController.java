package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.constant.BusinessErrorCode;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.vo.BookRecommend;
import cn.tannn.lychnos.entity.BookAnalyse;
import cn.tannn.lychnos.service.BookAnalyseService;
import cn.tannn.lychnos.service.UserInterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 书籍
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 11:10
 */

@PathRestController("book")
@Tag(name = "书籍")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookAnalyseService bookAnalyseService;
    private final UserInterestService userInterestService;

    /**
     * 模拟推荐数据（用于补充或无真实数据时返回）
     * <p>只要初始化了sql那就有这个</p>
     */
    private static final List<BookRecommend> MOCK_RECOMMENDATIONS = List.of(
            new BookRecommend(1L, "三体"),
            new BookRecommend(2L, "活着"),
            new BookRecommend(3L, "解忧杂货店"),
            new BookRecommend(4L, "奇特的一生"),
            new BookRecommend(5L, "宵待草夜情")
    );

    @Operation(summary = "书籍推荐", description = "基于用户兴趣推荐书籍")
    @ApiMapping(checkToken = false, value = "recommend", method = RequestMethod.GET)
    public ResultVO<List<BookRecommend>> recommend() {
        // 查询用户感兴趣的书籍（按感兴趣次数最多的前5本书）
        List<String> recommendBookTitles = userInterestService.getJpaBasicsDao()
                .findTop5BookTitlesByInterested(true);

        // 如果查询不到任何数据，返回模拟数据
        if (recommendBookTitles.isEmpty()) {
            log.info("未找到用户感兴趣的书籍，返回模拟推荐数据");
            return ResultVO.success(MOCK_RECOMMENDATIONS);
        }

        // 将书名转换为推荐对象（ID 按顺序生成）
        List<BookRecommend> resultRecommend = IntStream.range(0, recommendBookTitles.size())
                .mapToObj(i -> new BookRecommend((long) (i + 1), recommendBookTitles.get(i)))
                .collect(Collectors.toList());

        // 如果真实数据少于5条，用模拟数据补充（去重）
        if (resultRecommend.size() < 5) {
            log.info("真实推荐数据不足5条（当前{}条），使用模拟数据补充", resultRecommend.size());

            // 从模拟数据中筛选不重复的数据进行补充
            List<BookRecommend> supplemental = MOCK_RECOMMENDATIONS.stream()
                    .filter(mock -> !recommendBookTitles.contains(mock.getTitle()))
                    .limit(5L - resultRecommend.size())
                    .toList();

            // 合并真实数据和补充数据
            resultRecommend.addAll(supplemental);

            return ResultVO.success(resultRecommend);
        }

        log.info("返回{}条真实推荐数据", resultRecommend.size());
        return ResultVO.success(resultRecommend);
    }



    @Operation(summary = "公开分析图书", description = "无需登录即可查看推荐书籍的分析结果")
    @ApiMapping(checkToken = false, value = "analyze/public/{bookTitle}", method = RequestMethod.GET)
    public ResultVO<BookAnalyse> analyzePublic(@PathVariable("bookTitle") String bookTitle) {
        // 获取推荐书籍列表（真实数据 + 模拟数据）
        List<String> recommendBookTitles = getRecommendBookTitles();

        // 验证书名是否在推荐列表中
        validateBookInRecommendation(bookTitle, recommendBookTitles);
        // 查询并返回书籍分析记录
        BookAnalyse bookAnalyse = findBookAnalyseByTitle(bookTitle);

        log.info("公开分析接口返回书籍: {}", bookTitle);
        return ResultVO.success(bookAnalyse);
    }


    @Operation(summary = "分析图书", description = "根据书名进行分析图书")
    @PutMapping(value = "analyze/{bookTitle}")
    public ResultVO<BookAnalyse> analyze(@PathVariable("bookTitle") String bookTitle,
                                         HttpServletRequest request) {

        Long userId = UserUtil.userId2(request);

        // 检查是否已分析过，且图片是否完整
        var existingInterest = userInterestService.checkAnalyzed(userId, bookTitle);
        if (existingInterest.isPresent()) {
            // 查询书籍分析记录，检查图片是否存在
            var bookAnalyse = bookAnalyseService.findById(existingInterest.get().getBookAnalyseId());
            if (bookAnalyse.isPresent() &&
                bookAnalyse.get().getPosterUrl() != null &&
                !bookAnalyse.get().getPosterUrl().isEmpty()) {
                // 已分析过且图片完整，不允许重复分析
                throw new BusinessException(
                        BusinessErrorCode.BOOK_ALREADY_ANALYZED.getCode(),
                        BusinessErrorCode.BOOK_ALREADY_ANALYZED.getMessage()
                );
            }
            // 如果图片不存在，允许重新分析以补充生成图片
            log.info("书籍已分析但缺少图片，允许重新分析，书名: {}", bookTitle);
        }

        // 使用 AI 进行书籍分析（首次分析或补充图片）
        return ResultVO.success(bookAnalyseService.analyse(bookTitle, userId));
    }

    @Operation(summary = "检查书籍是否已分析", description = "根据书名检查当前用户是否已经分析过该书籍")
    @GetMapping(value = "check/{bookTitle}")
    public ResultVO<String> checkAnalyzed(@PathVariable("bookTitle") String bookTitle,
                                          HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);

        // 检查是否已分析过，且图片是否完整
        var existingInterest = userInterestService.checkAnalyzed(userId, bookTitle);
        if (existingInterest.isPresent()) {
            // 查询书籍分析记录，检查图片是否存在
            var bookAnalyse = bookAnalyseService.findById(existingInterest.get().getBookAnalyseId());
            if (bookAnalyse.isPresent() &&
                bookAnalyse.get().getPosterUrl() != null &&
                !bookAnalyse.get().getPosterUrl().isEmpty()) {
                // 已分析过且图片完整，不允许重复分析
                throw new BusinessException(
                        BusinessErrorCode.BOOK_ALREADY_ANALYZED.getCode(),
                        BusinessErrorCode.BOOK_ALREADY_ANALYZED.getMessage()
                );
            }
            // 如果图片不存在，返回可以分析（用于补充生成图片）
            log.info("书籍已分析但缺少图片，返回可以分析，书名: {}", bookTitle);
            return ResultVO.successMessage("该书籍图片缺失，可以重新分析补充图片");
        }

        return ResultVO.successMessage("当前用户为未分析该书籍，可以进行分析");
    }




    /**
     * 获取推荐书籍列表（真实数据 + 模拟数据）
     *
     * @return 推荐书籍标题列表
     */
    private List<String> getRecommendBookTitles() {
        List<String> recommendBookTitles = userInterestService.getJpaBasicsDao()
                .findTop5BookTitlesByInterested(true);

        // 如果推荐列表为空，返回模拟数据的书名
        if (recommendBookTitles.isEmpty()) {
            log.info("真实推荐数据为空，使用模拟数据");
            return MOCK_RECOMMENDATIONS.stream()
                    .map(BookRecommend::getTitle)
                    .collect(Collectors.toList());
        }

        return recommendBookTitles;
    }

    /**
     * 验证书名是否在推荐列表中
     *
     * @param bookTitle 书籍标题
     * @param recommendBookTitles 推荐书籍列表
     * @throws BusinessException 如果书籍不在推荐列表中
     */
    private void validateBookInRecommendation(String bookTitle, List<String> recommendBookTitles) {
        if (!recommendBookTitles.contains(bookTitle)) {
            log.warn("书籍不在推荐列表中: {}", bookTitle);
            throw new BusinessException(
                    BusinessErrorCode.BOOK_NOT_IN_RECOMMENDATION.getCode(),
                    BusinessErrorCode.BOOK_NOT_IN_RECOMMENDATION.getMessage()
            );
        }
    }

    /**
     * 根据书名查询书籍分析记录
     *
     * @param bookTitle 书籍标题
     * @return 书籍分析对象
     * @throws BusinessException 如果未找到分析数据
     */
    private BookAnalyse findBookAnalyseByTitle(String bookTitle) {
        return bookAnalyseService.getJpaBasicsDao()
                .findByTitle(bookTitle)
                .orElseThrow(() -> {
                    log.warn("未找到书籍分析数据: {}", bookTitle);
                    return new BusinessException(
                            BusinessErrorCode.BOOK_ANALYSIS_NOT_FOUND.getCode(),
                            BusinessErrorCode.BOOK_ANALYSIS_NOT_FOUND.getMessage()
                    );
                });
    }
}
