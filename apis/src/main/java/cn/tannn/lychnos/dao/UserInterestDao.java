package cn.tannn.lychnos.dao;

import cn.tannn.jdevelops.jpa.repository.JpaBasicsRepository;
import cn.tannn.lychnos.entity.UserInterest;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 用户兴趣
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 21:16
 */
public interface UserInterestDao extends JpaBasicsRepository<UserInterest, Long> {

    /**
     * 根据用户ID和书名查询用户兴趣
     * @param userId 用户ID
     * @param bookTitle 书名
     * @return 用户兴趣
     */
    Optional<UserInterest> findByUserIdAndBookTitle(Long userId, String bookTitle);

    /**
     * 查询感兴趣次数最多的书名（按 bookTitle 分组统计，返回次数最多的前5个书名）
     * @param interested 是否感兴趣
     * @return 书名列表（按感兴趣次数降序）
     */
    @Query("""
        SELECT ui.bookTitle FROM UserInterest ui
           WHERE ui.interested = :interested
           GROUP BY ui.bookTitle
           ORDER BY COUNT(ui.id) DESC
           LIMIT 5
                """)
    List<String> findTop5BookTitlesByInterested(Boolean interested);
}
