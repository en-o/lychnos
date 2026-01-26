package cn.tannn.lychnos.ai.prompt;

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

}
