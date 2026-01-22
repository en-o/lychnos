package cn.tannn.lychnos.controller.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI模型详情VO（包含用户名）
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/22
 */
@ToString
@Getter
@Setter
@Schema(description = "AI模型详情VO")
public class AIModelWithUserVO {

    @Schema(description = "模型ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "用户名")
    private String loginName;

    @Schema(description = "配置命名")
    private String name;

    @Schema(description = "模型名")
    private String model;

    @Schema(description = "api厂家")
    private String factory;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "模型类型")
    private String type;

    @Schema(description = "分享状态：0官方，1私人，2公开")
    private Integer share;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
