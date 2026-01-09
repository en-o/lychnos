package cn.tannn.lychnos.dao;

import cn.tannn.jdevelops.jpa.repository.JpaBasicsRepository;
import cn.tannn.lychnos.entity.UserInterest;

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
}
