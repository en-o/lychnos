package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.controller.dto.UserInterestFeedback;
import cn.tannn.lychnos.dao.UserInterestDao;
import cn.tannn.lychnos.entity.UserInterest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public UserInterestService() {
        super(UserInterest.class);
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
}
