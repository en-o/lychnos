package cn.tannn.lychnos.controller.vo;

import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.common.constant.UsageType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * 用户分析日志VO - JPA Projection接口
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/23
 */
public interface UserAnalysisLogVO {

    /**
     * 调用时间（createTime）
     */
    LocalDateTime getCreateTime();

    /**
     * 用户名
     */
    String getUserName();

    /**
     * 调用IP
     */
    String getCallIp();

    /**
     * 模型ID（作为tip显示）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    Long getModelId();

    /**
     * 模型名称
     */
    String getModelName();

    /**
     * 模型类型
     */
    ModelType getModelType();

    /**
     * 模型来源：0-官方/1-私人/2-公开
     */
    Integer getModelSource();

    /**
     * 用途类型
     */
    UsageType getUsageType();

    /**
     * 书籍标题
     */
    String getBookTitle();

    /**
     * 书籍分析ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    Long getBookAnalyseId();

    /**
     * 是否成功
     */
    Boolean getSuccess();

    /**
     * 错误信息
     */
    String getErrorMessage();

    /**
     * 是否使用已有数据
     */
    Boolean getUseExistingData();

    /**
     * 用户ID（作为tip显示）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    Long getUserId();

    /**
     * 模型厂商（作为tip显示）
     */
    String getModelVendor();
}
