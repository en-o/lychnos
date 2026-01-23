package cn.tannn.lychnos.dao;

import cn.tannn.lychnos.controller.vo.UserAnalysisLogVO;
import cn.tannn.lychnos.entity.UserAnalysisLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户分析日志
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
public interface UserAnalysisLogDao extends JpaRepository<UserAnalysisLog, Long> {

    /**
     * 通用查询方法 - 支持所有条件组合
     *
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param userName 用户名（可选）
     * @param modelSource 模型来源（可选）
     * @param pageable 分页参数
     * @return 日志VO列表
     */
    @Query("SELECT u FROM UserAnalysisLog u WHERE " +
           "(:startTime IS NULL OR u.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR u.createTime <= :endTime) AND " +
           "(:userName IS NULL OR u.userName = :userName) AND " +
           "(:modelSource IS NULL OR u.modelSource = :modelSource) " +
           "ORDER BY u.createTime DESC")
    List<UserAnalysisLogVO> findByConditions(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("userName") String userName,
                                              @Param("modelSource") Integer modelSource,
                                              Pageable pageable);

    /**
     * 通用查询方法 - 支持用户名模糊匹配
     *
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param userName 用户名（可选，模糊匹配）
     * @param modelSource 模型来源（可选）
     * @param pageable 分页参数
     * @return 日志VO列表
     */
    @Query("SELECT u FROM UserAnalysisLog u WHERE " +
           "(:startTime IS NULL OR u.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR u.createTime <= :endTime) AND " +
           "(:userName IS NULL OR u.userName LIKE %:userName%) AND " +
           "(:modelSource IS NULL OR u.modelSource = :modelSource) " +
           "ORDER BY u.createTime DESC")
    List<UserAnalysisLogVO> findByConditionsWithUserNameLike(@Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime,
                                                              @Param("userName") String userName,
                                                              @Param("modelSource") Integer modelSource,
                                                              Pageable pageable);
}
