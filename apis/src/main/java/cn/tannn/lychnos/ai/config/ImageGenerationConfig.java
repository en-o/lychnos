package cn.tannn.lychnos.ai.config;

import lombok.Builder;
import lombok.Getter;

/**
 * Image 生成配置
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/26
 */
@Getter
@Builder
public class ImageGenerationConfig {

    /**
     * 图片默认宽度（像素）
     */
    @Builder.Default
    private Integer defaultWidth = 1024;

    /**
     * 图片默认高度（像素）
     */
    @Builder.Default
    private Integer defaultHeight = 1024;

    /**
     * 图片生成推理步数（Z-Image-Turbo 推荐值为 9）
     */
    @Builder.Default
    private Integer inferenceSteps = 9;

    /**
     * 图片生成引导系数（Turbo 模型必须为 0.0）
     */
    @Builder.Default
    private Double guidanceScale = 0.0;

    /**
     * 图片生成随机种子（-1 表示随机，固定值可保证可复现性）
     */
    @Builder.Default
    private Integer seed = 42;

    /**
     * 图片生成数量
     */
    @Builder.Default
    private Integer count = 1;

    /**
     * 图片质量（如: "standard", "hd"）
     */
    @Builder.Default
    private String quality = "standard";

    /**
     * 图片风格（如: "vivid", "natural"）
     */
    @Builder.Default
    private String style = "vivid";

    /**
     * 获取默认尺寸字符串（格式: "宽x高"）
     *
     * @return 尺寸字符串，如 "1024x1024"
     */
    public String buildSizeString() {
        return defaultWidth + "x" + defaultHeight;
    }

    /**
     * 构建尺寸字符串（格式: "宽x高"）
     *
     * @param width  宽度
     * @param height 高度
     * @return 尺寸字符串
     */
    public static String buildSizeString2(Integer width, Integer height) {
        return width + "x" + height;
    }

    /**
     * 解析尺寸字符串
     *
     * @param sizeString 尺寸字符串（格式: "宽x高"）
     * @return [宽度, 高度]
     */
    public static int[] parseSizeString(String sizeString) {
        String[] parts = sizeString.split("x");
        if (parts.length == 2) {
            try {
                return new int[]{
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1])
                };
            } catch (NumberFormatException e) {
                return new int[]{1024, 1024};
            }
        }
        return new int[]{1024, 1024};
    }
}

