package cn.tannn.lychnos.entity;

import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.common.pojo.JpaCommonBean;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * AI模型
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/10 10:00
 */
@Entity
@Table(name = "tb_ai_model", indexes = {
        @Index(name = "idx_user_type_enabled", columnList = "userId,enabled,type"),
        @Index(name = "idx_user_enabled", columnList = "userId,type"),
})
@Comment("AI模型")
@Getter
@Setter
@ToString
@DynamicUpdate
@DynamicInsert
@Schema(description = "AI模型")
public class AIModel extends JpaCommonBean<AIModel> {

    /**
     * 用户id, 登录名可能存在更改的问题，id是不是自增的所有是稳定的
     */
    @Column(columnDefinition = " varchar(100) not null ")
    @Comment("用户id")
    @Schema(description = "用户id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 配置命名
     */
    @Column(columnDefinition = " varchar(500)", nullable = false)
    @Comment("配置命名")
    @Schema(description = "配置命名")
    private String name;

    /**
     * 模型名
     */
    @Column(columnDefinition = " varchar(500)", nullable = false)
    @Comment("模型名")
    @Schema(description = "模型名")
    private String model;

    /**
     * api厂家
     * 文本分析模型: openai | ollama | deepseek | azure | anthropic | qwen | baidu | modelscope | huggingface
     * 图片生成模型: stable-diffusion | midjourney | dall-e | nano-banana-pro | modelscope-image | huggingface-image
     * 通用: custom (自定义)
     */
    @Column(columnDefinition = " varchar(500)", nullable = false)
    @Comment("api厂家:openai|ollama|deepseek|azure|anthropic|qwen|baidu|modelscope|huggingface|stable-diffusion|midjourney|dall-e|nano-banana-pro|modelscope-image|huggingface-image|custom")
    @Schema(description = "api厂家:openai|ollama|deepseek|azure|anthropic|qwen|baidu|modelscope|huggingface|stable-diffusion|midjourney|dall-e|nano-banana-pro|modelscope-image|huggingface-image|custom")
    private String factory;

    /**
     * apiKey[key是可以空的比如ollama]
     */
    @Column(columnDefinition = " varchar(500)")
    @Comment("apiKey[key是可以空的比如ollama]")
    @Schema(description = "apiKey[key是可以空的比如ollama]")
    private String apiKey;


    /**
     * apiUrl
     */
    @Column(columnDefinition = " varchar(500)", nullable = false)
    @Comment("apiUrl")
    @Schema(description = "apiUrl")
    private String apiUrl;

    /**
     * 是否启用 - 一种类型只能启用一个
     * <p> 过期 直接抛异常不用设计字段处理</p>
     */
    @Column(columnDefinition = " tinyint(1) ")
    @Comment("是否启用[0:false, 1:true]")
    @Schema(description = "是否启用")
    private Boolean enabled;

    /**
     * 模型类型：当前就两个图片和文字
     */
    @Column(columnDefinition = " varchar(20) ", nullable = false)
    @Comment("模型类型：当前就两个图片和文字")
    @Schema(description = "是否模型类型：当前就两个图片和文字")
    @Enumerated(EnumType.STRING)
    private ModelType type;

}
