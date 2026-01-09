package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.vo.BookRecommend;
import cn.tannn.lychnos.entity.BookAnalyse;
import cn.tannn.lychnos.entity.UserInterest;
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

    @Operation(summary = "书籍推荐", description = "分析输入看下面的 试试:")
    @ApiMapping(checkToken = false, value = "recommend", method = RequestMethod.GET)
    public ResultVO<List<BookRecommend>> recommend() {
        return ResultVO.success(List.of(
                new BookRecommend(1L, "三体"),
                new BookRecommend(2L, "活着"),
                new BookRecommend(3L, "解忧杂货店"),
                new BookRecommend(4L, "人类简史"),
                new BookRecommend(5L, "宵待草夜情")));
    }


    @Operation(summary = "分析图书", description = "根据书名进行分析图书")
    @PutMapping(value = "analyze/{bookTitle}")
    public ResultVO<BookAnalyse> analyze(@PathVariable("bookTitle") String bookTitle,
                                         HttpServletRequest request) {

        Long userId = UserUtil.userId2(request);
        if (userInterestService.checkAnalyzed(userId, bookTitle).isPresent()) {
            throw new BusinessException(1001, "该书籍已经分析过，请到历史记录中查看");
        }
        ;
        // 具体分析模型后面做，现在用内置的假数据
        return ResultVO.success(bookAnalyseService.analyse(bookTitle));
    }

    @Operation(summary = "检查书籍是否已分析", description = "根据书名检查当前用户是否已经分析过该书籍")
    @GetMapping(value = "check/{bookTitle}")
    public ResultVO<String> checkAnalyzed(@PathVariable("bookTitle") String bookTitle,
                                          HttpServletRequest request) {
        Long userId = UserUtil.userId2(request);
        if (userInterestService.checkAnalyzed(userId, bookTitle).isPresent()) {
            throw new BusinessException(1001, "该书籍已经分析过，请到历史记录中查看");
        }
        return ResultVO.successMessage("该书籍未分析，可以进行分析");
    }
}
