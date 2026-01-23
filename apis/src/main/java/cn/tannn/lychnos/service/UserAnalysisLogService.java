package cn.tannn.lychnos.service;

import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.common.constant.UsageType;
import cn.tannn.lychnos.common.pojo.UserRequestInfo;
import cn.tannn.lychnos.controller.dto.UserAnalysisLogQueryDTO;
import cn.tannn.lychnos.controller.vo.UserAnalysisLogVO;
import cn.tannn.lychnos.dao.UserAnalysisLogDao;
import cn.tannn.lychnos.entity.AIModel;
import cn.tannn.lychnos.entity.UserAnalysisLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户分析日志服务
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAnalysisLogService {

    private final UserAnalysisLogDao userAnalysisLogDao;

    /**
     * 保存书籍提取日志
     */
    @Async
    public void saveExtractLog(Long userId, UserRequestInfo userInfo, AIModel model,
                               String bookTitle, Long bookAnalyseId, boolean success, String errorMessage) {
        UserAnalysisLog log = new UserAnalysisLog();
        log.setUserId(userId);
        log.setUserName(userInfo != null ? userInfo.getUsername() : null);
        log.setCallIp(userInfo != null ? userInfo.getIp() : null);
        log.setUsageType(UsageType.BOOK_EXTRACT);
        log.setBookTitle(bookTitle);
        log.setBookAnalyseId(bookAnalyseId);
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);
        log.setUseExistingData(false);

        if (model != null) {
            log.setModelId(model.getId());
            log.setModelName(model.getModel());
            log.setModelVendor(model.getFactory());
            log.setModelType(ModelType.TEXT);
            log.setModelSource(model.getShare());
        }

        userAnalysisLogDao.save(log);
    }

    /**
     * 保存书籍解析日志
     */
    @Async
    public void saveParseLog(Long userId, UserRequestInfo userInfo, AIModel model,
                             String bookTitle, Long bookAnalyseId, boolean success, String errorMessage) {
        UserAnalysisLog log = new UserAnalysisLog();
        log.setUserId(userId);
        log.setUserName(userInfo != null ? userInfo.getUsername() : null);
        log.setCallIp(userInfo != null ? userInfo.getIp() : null);
        log.setUsageType(UsageType.BOOK_PARSE);
        log.setBookTitle(bookTitle);
        log.setBookAnalyseId(bookAnalyseId);
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);
        log.setUseExistingData(false);

        if (model != null) {
            log.setModelId(model.getId());
            log.setModelName(model.getModel());
            log.setModelVendor(model.getFactory());
            log.setModelType(ModelType.TEXT);
            log.setModelSource(model.getShare());
        }

        userAnalysisLogDao.save(log);
    }

    /**
     * 保存书籍生图日志
     */
    @Async
    public void saveImageLog(Long userId, UserRequestInfo userInfo, AIModel model,
                             String bookTitle, Long bookAnalyseId, boolean success, String errorMessage) {
        UserAnalysisLog log = new UserAnalysisLog();
        log.setUserId(userId);
        log.setUserName(userInfo != null ? userInfo.getUsername() : null);
        log.setCallIp(userInfo != null ? userInfo.getIp() : null);
        log.setUsageType(UsageType.BOOK_IMAGE);
        log.setBookTitle(bookTitle);
        log.setBookAnalyseId(bookAnalyseId);
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);
        log.setUseExistingData(false);

        if (model != null) {
            log.setModelId(model.getId());
            log.setModelName(model.getModel());
            log.setModelVendor(model.getFactory());
            log.setModelType(ModelType.IMAGE);
            log.setModelSource(model.getShare());
        }

        userAnalysisLogDao.save(log);
    }

    /**
     * 保存使用已有数据的日志（模型字段为空）
     */
    @Async
    public void saveUseExistingDataLog(Long userId, UserRequestInfo userInfo,
                                       String bookTitle, Long bookAnalyseId) {
        UserAnalysisLog log = new UserAnalysisLog();
        log.setUserId(userId);
        log.setUserName(userInfo != null ? userInfo.getUsername() : null);
        log.setCallIp(userInfo != null ? userInfo.getIp() : null);
        log.setUsageType(UsageType.BOOK_PARSE);
        log.setBookTitle(bookTitle);
        log.setBookAnalyseId(bookAnalyseId);
        log.setSuccess(true);
        log.setUseExistingData(true);

        userAnalysisLogDao.save(log);
    }

    /**
     * 查询用户分析日志（管理员功能）
     * 默认查询最近20条，最多返回200条
     * 直接返回JPA Projection接口
     * 支持模型来源筛选
     *
     * @param queryDTO 查询条件
     * @return 日志VO列表
     */
    public List<UserAnalysisLogVO> queryLogs(UserAnalysisLogQueryDTO queryDTO) {
        // 默认查询20条，最多200条
        int limit = 20;
        if (queryDTO.getStartTime() != null || queryDTO.getEndTime() != null
            || StringUtils.isNotBlank(queryDTO.getUserName()) || queryDTO.getModelSource() != null) {
            limit = 200;
        }
        Pageable pageable = PageRequest.of(0, limit);

        // 使用通用查询方法
        if (Boolean.TRUE.equals(queryDTO.getExactMatch())) {
            // 精确匹配用户名
            return userAnalysisLogDao.findByConditions(
                    queryDTO.getStartTime(),
                    queryDTO.getEndTime(),
                    queryDTO.getUserName(),
                    queryDTO.getModelSource(),
                    pageable);
        } else {
            // 模糊匹配用户名
            return userAnalysisLogDao.findByConditionsWithUserNameLike(
                    queryDTO.getStartTime(),
                    queryDTO.getEndTime(),
                    queryDTO.getUserName(),
                    queryDTO.getModelSource(),
                    pageable);
        }
    }
}
