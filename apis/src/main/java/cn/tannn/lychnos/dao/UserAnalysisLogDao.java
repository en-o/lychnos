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
     * 查询最近的日志记录（按创建时间倒序）- 返回投影接口
     *
     * @param pageable 分页参数
     * @return 日志VO列表
     */
    List<UserAnalysisLogVO> findAllByOrderByCreateTimeDesc(Pageable pageable);

    /**
     * 根据时间范围查询日志（按创建时间倒序）- 返回投影接口
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 日志VO列表
     */
    @Query("SELECT u FROM UserAnalysisLog u WHERE u.createTime >= :startTime AND u.createTime <= :endTime ORDER BY u.createTime DESC")
    List<UserAnalysisLogVO> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime,
                                                     Pageable pageable);

    /**
     * 根据用户名精确匹配查询日志（按创建时间倒序）- 返回投影接口
     *
     * @param userName 用户名
     * @param pageable 分页参数
     * @return 日志VO列表
     */
    List<UserAnalysisLogVO> findByUserNameOrderByCreateTimeDesc(String userName, Pageable pageable);

    /**
     * 根据用户名模糊匹配查询日志（按创建时间倒序）- 返回投影接口
     *
     * @param userName 用户名
     * @param pageable 分页参数
     * @return 日志VO列表
     */
    List<UserAnalysisLogVO> findByUserNameContainingOrderByCreateTimeDesc(String userName, Pageable pageable);

    /**
     * 根据用户名精确匹配和时间范围查询日志（按创建时间倒序）- 返回投影接口
     *
     * @param userName 用户名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 日志VO列表
     */
    @Query("SELECT u FROM UserAnalysisLog u WHERE u.userName = :userName AND u.createTime >= :startTime AND u.createTime <= :endTime ORDER BY u.createTime DESC")
    List<UserAnalysisLogVO> findByUserNameAndCreateTimeBetween(@Param("userName") String userName,
                                                                @Param("startTime") LocalDateTime startTime,
                                                                @Param("endTime") LocalDateTime endTime,
                                                                Pageable pageable);

    /**
     * 根据用户名模糊匹配和时间范围查询日志（按创建时间倒序）- 返回投影接口
     *
     * @param userName 用户名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 日志VO列表
     */
    @Query("SELECT u FROM UserAnalysisLog u WHERE u.userName LIKE %:userName% AND u.createTime >= :startTime AND u.createTime <= :endTime ORDER BY u.createTime DESC")
    List<UserAnalysisLogVO> findByUserNameContainingAndCreateTimeBetween(@Param("userName") String userName,
                                                                          @Param("startTime") LocalDateTime startTime,
                                                                          @Param("endTime") LocalDateTime endTime,
                                                                          Pageable pageable);
}
