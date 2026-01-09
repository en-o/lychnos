package cn.tannn.lychnos.dao;

import cn.tannn.jdevelops.jpa.repository.JpaBasicsRepository;
import cn.tannn.lychnos.entity.BookAnalyse;

import java.util.Optional;

/**
 * 书籍分析
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 21:15
 */
public interface BookAnalyseDao  extends JpaBasicsRepository<BookAnalyse, Long> {


    /**
     * 根据书名查询书籍分析
     * @param bookTitle 书名
     * @return 已经分析的书籍
     */
    Optional<BookAnalyse> findByTitle(String bookTitle);


}
