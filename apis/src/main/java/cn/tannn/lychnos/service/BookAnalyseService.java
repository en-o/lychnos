package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.dao.BookAnalyseDao;
import cn.tannn.lychnos.entity.BookAnalyse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 书籍分析
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 21:18
 */
@Service
@Slf4j
public class BookAnalyseService extends J2ServiceImpl<BookAnalyseDao, BookAnalyse, Long> {

    public BookAnalyseService() {
        super(BookAnalyse.class);
    }

    /**
     * 根据书名分析书籍
     * @param bookTitle 书名
     * @return 书籍分析
     */
    public BookAnalyse analyse(String bookTitle){
        // 如果分析过了直接返回的，没有的则进行分析处理，当前暂不对接ai直接抛异常
        return getJpaBasicsDao().findByTitle(bookTitle)
                .orElseThrow(() -> new BusinessException("分析功能暂未实现，只能使用提供的书籍进行测试"));

    }

    /**
     * 根据id查询书籍分析
     * @param bookAnalyseId BookAnalyse
     * @return BookAnalyse
     */
    public Optional<BookAnalyse> findById(Long bookAnalyseId) {
        return getJpaBasicsDao().findById(bookAnalyseId);
    }
}
