package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.controller.vo.BookRecommend;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Operation(summary = "书籍推荐",description = "分析输入看下面的 试试:")
    @ApiMapping(checkToken = false,value = "recommend",method = RequestMethod.GET)
    public ResultVO<List<BookRecommend>> recommend(){
        return ResultVO.success(List.of(
                new BookRecommend(1L,"三体"),
                new BookRecommend(2L,"活着"),
                new BookRecommend(3L,"解忧杂货铺"),
                new BookRecommend(4L,"人类简史"),
                new BookRecommend(5L,"宵待草夜情")));
    }
}
