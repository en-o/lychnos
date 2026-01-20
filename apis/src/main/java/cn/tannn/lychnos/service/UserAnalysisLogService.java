package cn.tannn.lychnos.service;

import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.common.constant.UsageType;
import cn.tannn.lychnos.dao.UserAnalysisLogDao;
import cn.tannn.lychnos.entity.AIModel;
import cn.tannn.lychnos.entity.UserAnalysisLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
     * 保存书籍解析日志
     */
    @Async
    public void saveParseLog(Long userId, String userName, String callIp,
                             AIModel model, String bookTitle, Long bookAnalyseId,
                             boolean success, String errorMessage) {
        UserAnalysisLog log = new UserAnalysisLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setCallIp(callIp);
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
    public void saveImageLog(Long userId, String userName, String callIp,
                             AIModel model, String bookTitle, Long bookAnalyseId,
                             boolean success, String errorMessage) {
        UserAnalysisLog log = new UserAnalysisLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setCallIp(callIp);
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
    public void saveUseExistingDataLog(Long userId, String userName, String callIp,
                                       String bookTitle, Long bookAnalyseId) {
        UserAnalysisLog log = new UserAnalysisLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setCallIp(callIp);
        log.setUsageType(UsageType.BOOK_PARSE);
        log.setBookTitle(bookTitle);
        log.setBookAnalyseId(bookAnalyseId);
        log.setSuccess(true);
        log.setUseExistingData(true);

        userAnalysisLogDao.save(log);
    }

    /**
     * 从请求中获取真实IP
     */
    public static String getRealIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
