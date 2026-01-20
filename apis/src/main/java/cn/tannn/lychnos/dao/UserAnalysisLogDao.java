package cn.tannn.lychnos.dao;

import cn.tannn.lychnos.entity.UserAnalysisLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户分析日志
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
public interface UserAnalysisLogDao extends JpaRepository<UserAnalysisLog, Long> {
}
