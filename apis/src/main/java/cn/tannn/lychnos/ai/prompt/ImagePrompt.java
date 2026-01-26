package cn.tannn.lychnos.ai.prompt;

import cn.tannn.lychnos.entity.BookAnalyse;

/**
 * 图片提示词
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/26 14:15
 */
public class ImagePrompt {


    /**
     * 默认图片风格提示词
     * 风格：现代信息图解风格1920x1080尺寸
     * 强制横向布局，内容密集
     */
    public static final String DEFAULT_IMAGE_STYLE_PROMPT = """
            CRITICAL REQUIREMENTS (MUST FOLLOW):
            - Image size: EXACTLY 1920x1080 pixels (16:9 aspect ratio)
            - Orientation: HORIZONTAL LANDSCAPE (width MUST be 1024, height MUST be 576)
            - DO NOT create vertical/portrait images

            Style Requirements:
            - Design style: Modern infographic poster with dense, information-rich layout
            - Background: Light neutral color (beige, light gray, or white) suitable for reading
            - Color scheme: Harmonious color palette with clear contrast for readability
            - Layout: Multi-section horizontal layout with maximum information density
            - Typography: Clean, modern fonts; mix of bold headers and regular body text
            - Visual elements: Multiple illustrations, icons, diagrams, charts to fill the space
            - Composition: Divide horizontally into 2-3 main columns or sections
            - Information density: HIGH - utilize all available horizontal space
            - Decorative elements: Minimal, focus on information delivery
            - Atmosphere: Professional, educational, informative
            - Quality: High resolution, suitable for web display
            - Information presentation: Use boxes, arrows, bullet points, numbered lists, and visual connectors

            Layout Suggestions:
            - Left section: Main title and key information
            - Middle section: Core content with icons/diagrams
            - Right section: Additional details or summary
            - Use horizontal dividers and borders to organize content

            """;

    /**
     * 构建书籍信息图内容提示词
     *
     * @param analysis 书籍分析对象
     * @return 图片内容提示词
     */
    public static String buildBookInfographicPrompt(BookAnalyse analysis) {
        // 将JSONArray转换为字符串列表
        String themesStr = analysis.getThemes() != null ?
                String.join("、", analysis.getThemes().toJavaList(String.class)) : "";
        String keyElementsStr = analysis.getKeyElements() != null ?
                String.join("、", analysis.getKeyElements().toJavaList(String.class)) : "";

        // 提取推荐语的核心内容（前80字）作为补充描述
        String shortRecommendation = "";
        if (analysis.getRecommendation() != null && !analysis.getRecommendation().isEmpty()) {
            shortRecommendation = analysis.getRecommendation().length() > 80 ?
                    analysis.getRecommendation().substring(0, 80) + "..." :
                    analysis.getRecommendation();
        }

        // 提示词
        return String.format("""
                【书籍信息】
                书名：《%s》
                类型：%s
                基调：%s
                核心主题：%s
                关键元素：%s

                【内容简介】
                %s

                【设计要求】
                请创建一张书籍内容分析信息图（非封面图），重点展示书籍的核心信息和分析结果：
                1. 标识信息：
                   - 书名标识：《%s》（简洁展示，不占用过多空间）
                2. 核心内容展示（主要部分）：
                   - 类型标签：【%s】用彩色标签展示
                   - 基调说明：%s，用副标题或图标展示
                   - 主题展示：%s，用图标或色块分区展示
                   - 关键元素：%s，用插图或符号可视化
                3. 视觉风格：
                   - 根据书籍类型选择合适的配色方案（科幻：蓝紫色系；文学：暖色系；历史：典雅色系等）
                   - 使用扁平化设计，信息层次清晰
                   - 添加相关的图标、符号或简笔插画
                   - 保持横向16:9布局(1920x1080 Full HD高分辨率)，信息密度高但不拥挤
                4. 内容布局：
                   - 将整个画面分为2-3个水平区域
                   - 每个区域展示不同维度的书籍分析信息
                   - 使用分隔线、色块或留白区分各区域
                   - 重点突出内容分析，而非书名

                【中文文字要求（极其重要）】
                ⚠️ CRITICAL: 生成的信息图中如果包含中文文字，必须确保每个中文字符都是真实存在的汉字，字形清晰准确。
                ⚠️ 严禁生成类似汉字但实际上不存在或无意义的字符（乱码）。
                ⚠️ 如果无法准确生成中文字符，请改用图标、符号或英文代替。
                ⚠️ 建议：尽可能使用视觉元素（图标、色块、插画）表达信息，减少中文文字的使用。

                【注意事项】
                - 这是内容分析信息图，不是封面图，重点展示分析内容
                - 所有文字（中文/英文/其他语言）必须清晰、准确、真实存在
                - 色彩搭配和谐，符合书籍主题
                - 信息完整但不凌乱
                - 整体风格现代、专业、吸引人
                """,
                analysis.getTitle(),
                analysis.getGenre(),
                analysis.getTone(),
                themesStr,
                keyElementsStr,
                shortRecommendation,
                analysis.getTitle(),
                analysis.getGenre(),
                analysis.getTone(),
                themesStr,
                keyElementsStr
        );
    }

}
