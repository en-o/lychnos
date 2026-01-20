package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.constant.BusinessErrorCode;
import cn.tannn.lychnos.common.util.SignedUrlUtil;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.dto.BookExtractDTO;
import cn.tannn.lychnos.controller.vo.BookExtractVO;
import cn.tannn.lychnos.controller.vo.BookRecommend;
import cn.tannn.lychnos.entity.BookAnalyse;
import cn.tannn.lychnos.service.BookAnalyseService;
import cn.tannn.lychnos.service.UserInterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.ArrayList;
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

    @Value("${app.security.aes-secret-key}")
    private String secretKey;

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
        // 获取推荐书籍列表（真实数据 + 模拟数据补充）
        List<String> recommendBookTitles = getRecommendBookTitles();

        // 将书名转换为推荐对象（ID 按顺序生成）
        List<BookRecommend> resultRecommend = IntStream.range(0, recommendBookTitles.size())
                .mapToObj(i -> new BookRecommend((long) (i + 1), recommendBookTitles.get(i)))
                .collect(Collectors.toList());

        log.info("返回{}条推荐数据", resultRecommend.size());
        return ResultVO.success(resultRecommend);
    }


    @Operation(summary = "查询书籍分析结果", description = "查询书籍分析结果，已登录用户会检查是否已反馈，未登录用户只能查看推荐书籍")
    @ApiMapping(checkToken = false, value = "query/{bookTitle}", method = RequestMethod.GET)
    public ResultVO<BookAnalyse> queryBookAnalysis(@PathVariable("bookTitle") String bookTitle,
                                                   HttpServletRequest request) {
        Long userId = null;
        try {
            userId = UserUtil.userId2(request);
        } catch (Exception e) {
            // Token 不存在或无效，userId 保持为 null
            log.debug("获取用户ID失败，可能是未登录用户: {}", e.getMessage());
        }

        bookTitle = bookTitle.trim();

        if (userId != null) {
            // 已登录用户：检查是否已分析过，且图片是否完整
            var existingInterest = userInterestService.checkAnalyzed(userId, bookTitle);
            if (existingInterest.isPresent()) {
                // 查询书籍分析记录，检查图片是否存在
                var bookAnalyse = bookAnalyseService.findById(existingInterest.get().getBookAnalyseId());
                if (bookAnalyse.isPresent() &&
                    bookAnalyse.get().getPosterUrl() != null &&
                    !bookAnalyse.get().getPosterUrl().isEmpty()) {
                    // 已分析过且图片完整，直接返回分析结果
                    log.info("用户已分析过该书籍，返回分析结果，书名: {}", bookTitle);
                    return ResultVO.success(bookAnalyse.get());
                }
                // 如果图片不存在，返回null表示可以分析（用于补充生成图片）
                log.info("书籍已分析但缺少图片，返回null表示可以分析，书名: {}", bookTitle);
                return ResultVO.success(null);
            }

            // 未分析过，返回null
            return ResultVO.success(null);
        } else {
            // 未登录用户：只能查看推荐书籍
            List<String> recommendBookTitles = getRecommendBookTitles();

            // 验证书名是否在推荐列表中
            validateBookInRecommendation(bookTitle, recommendBookTitles);
            // 查询并返回书籍分析记录
            BookAnalyse bookAnalyse = findBookAnalyseByTitle(bookTitle);

            // 为未登录用户的图片 URL 添加签名（30分钟有效期）
            if (bookAnalyse.getPosterUrl() != null && !bookAnalyse.getPosterUrl().isEmpty()) {
                String signedParams = SignedUrlUtil.generateSignature(bookAnalyse.getPosterUrl(), secretKey);
                // 将签名参数附加到 posterUrl（前端会在请求图片时使用）
                bookAnalyse.setPosterUrl(bookAnalyse.getPosterUrl() + "?" + signedParams);
                log.info("为未登录用户生成签名 URL，书名: {}", bookTitle);
            }

            log.info("未登录用户查询推荐书籍: {}", bookTitle);
            return ResultVO.success(bookAnalyse);
        }
    }


    @Operation(summary = "分析图书", description = "根据书名和作者进行分析图书")
    @PutMapping(value = "analyze")
    public ResultVO<BookAnalyse> analyze(@RequestBody BookExtractVO bookInfo,
                                         HttpServletRequest request) {

        Long userId = UserUtil.userId2(request);

        String bookTitle = bookInfo.getTitle().trim();
        String author = bookInfo.getAuthor();

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
        return ResultVO.success(bookAnalyseService.analyse(bookTitle, author, userId));
    }

    @Operation(summary = "提取书籍信息", description = "从用户输入中提取书名和作者信息")
    @PostMapping(value = "extract")
    public ResultVO<List<BookExtractVO>> extractBooks(@RequestBody BookExtractDTO dto,
                                                      HttpServletRequest request) {
        Long userId = null;
        try {
            userId = UserUtil.userId2(request);
        } catch (Exception e) {
            // Token 不存在或无效
            log.debug("获取用户ID失败，可能是未登录用户: {}", e.getMessage());
            throw new BusinessException(
                    BusinessErrorCode.PARAM_ERROR.getCode(),
                    "需要登录才能使用书籍提取功能"
            );
        }

        if (dto.getInput() == null || dto.getInput().trim().isEmpty()) {
            throw new BusinessException(
                    BusinessErrorCode.PARAM_ERROR.getCode(),
                    "输入内容不能为空"
            );
        }

        // 使用AI提取书籍信息
        List<BookExtractVO> books = bookAnalyseService.extractBooks(dto.getInput(), userId);

        // 检查每本书是否已分析过
        for (BookExtractVO book : books) {
            var existingInterest = userInterestService.checkAnalyzed(userId, book.getTitle());
            if (existingInterest.isPresent()) {
                var bookAnalyse = bookAnalyseService.findById(existingInterest.get().getBookAnalyseId());
                if (bookAnalyse.isPresent() &&
                    bookAnalyse.get().getPosterUrl() != null &&
                    !bookAnalyse.get().getPosterUrl().isEmpty()) {
                    book.setAnalyzed(true);
                }
            }
        }

        log.info("提取到{}本书籍信息", books.size());
        return ResultVO.success(books);
    }


    /**
     * 获取推荐书籍列表（真实数据 + 模拟数据补充）
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

        // 如果真实数据少于5条，用模拟数据补充（去重）
        if (recommendBookTitles.size() < 5) {
            log.info("真实推荐数据不足5条（当前{}条），使用模拟数据补充", recommendBookTitles.size());
            List<String> supplementalTitles = MOCK_RECOMMENDATIONS.stream()
                    .map(BookRecommend::getTitle)
                    .filter(title -> !recommendBookTitles.contains(title))
                    .limit(5L - recommendBookTitles.size())
                    .toList();

            List<String> allTitles = new ArrayList<>(recommendBookTitles);
            allTitles.addAll(supplementalTitles);
            return allTitles;
        }

        return recommendBookTitles;
    }

    /**
     * 验证书名是否在推荐列表中
     *
     * @param bookTitle           书籍标题
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
