package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.ai.service.AIService;
import cn.tannn.lychnos.dao.BookAnalyseDao;
import cn.tannn.lychnos.entity.BookAnalyse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

/**
 * 书籍分析
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/9 21:18
 */
@Service
@Slf4j
public class BookAnalyseService extends J2ServiceImpl<BookAnalyseDao, BookAnalyse, Long> {

    private final AIService aiService;
    private final ImageStorageService imageStorageService;

    public BookAnalyseService(AIService aiService, ImageStorageService imageStorageService) {
        super(BookAnalyse.class);
        this.aiService = aiService;
        this.imageStorageService = imageStorageService;
    }

    /**
     * 根据书名分析书籍
     * @param bookTitle 书名
     * @param userId 用户ID
     * @return 书籍分析
     */
    public BookAnalyse analyse(String bookTitle, Long userId){
        // 先查询是否已经分析过
        Optional<BookAnalyse> existingAnalyse = getJpaBasicsDao().findByTitle(bookTitle);
        if (existingAnalyse.isPresent()) {
            log.info("书籍已分析过，直接返回，书名: {}", bookTitle);
            return existingAnalyse.get();
        }

        // 使用AI进行分析
        log.info("开始AI分析书籍，书名: {}, 用户ID: {}", bookTitle, userId);

        String prompt = buildAnalysisPrompt(bookTitle);
        String aiResponse = aiService.generateText(userId, prompt);

        // 解析AI响应并保存
        BookAnalyse bookAnalyse = parseAIResponse(bookTitle, aiResponse);

        // 生成书籍分析信息图（必须成功）
        log.info("开始生成书籍分析信息图，书名: {}", bookTitle);
        String imageContentPrompt = buildImageContentPrompt(bookAnalyse);

        // 生成图片流
        try (InputStream imageStream = aiService.generateImageStreamWithContent(userId, imageContentPrompt)) {
            if (imageStream == null) {
                log.error("AI 返回的图片流为 null，书名: {}", bookTitle);
                throw new RuntimeException("图览信息生成失败，请检查生图模型的配置和网络环境");
            }

            // 保存图片到本地，返回 posterUrl
            String posterUrl = imageStorageService.saveImage(imageStream, bookTitle);
            bookAnalyse.setPosterUrl(posterUrl);
            log.info("书籍分析信息图生成并保存成功，posterUrl: {}", posterUrl);
        } catch (Exception e) {
            log.error("书籍分析信息图生成失败，书名: {}, 错误: {}", bookTitle, e.getMessage(), e);
            // 图片生成是必须的，失败则抛出异常
            throw new RuntimeException("图览信息生成失败，请检查生图模型的配置和网络环境。详细错误: " + e.getMessage(), e);
        }

        BookAnalyse saved = getJpaBasicsDao().save(bookAnalyse);

        log.info("书籍分析完成并保存，书名: {}", bookTitle);
        return saved;
    }

    /**
     * 根据id查询书籍分析
     * @param bookAnalyseId BookAnalyse
     * @return BookAnalyse
     */
    public Optional<BookAnalyse> findById(Long bookAnalyseId) {
        return getJpaBasicsDao().findById(bookAnalyseId);
    }

    /**
     * 构建分析提示词
     */
    private String buildAnalysisPrompt(String bookTitle) {
        return String.format("""
                你是一位资深的图书评论专家和文学研究者。请对书籍《%s》进行全面而精准的分析。

                重要说明：
                1. 请基于你的知识库中关于这本书的信息进行分析
                2. 如果这本书在你的知识库中，请提供准确详实的分析
                3. 如果不确定或不了解这本书，请明确说明，不要编造内容

                请以JSON格式返回分析结果，格式如下：
                {
                  "genre": "类型/流派（10字以内），如：科幻小说、历史传记、哲学著作、经济学、心理学等",
                  "themes": ["主题1（5字以内）", "主题2（5字以内）", "主题3（5字以内）"],
                  "tone": "基调（8字以内），如：严肃深刻、轻松幽默、感人至深、理性客观等",
                  "keyElements": ["关键要素1（8字以内）", "关键要素2（8字以内）", "关键要素3（8字以内）"],
                  "recommendation": "书籍综述和推荐理由，200-300字"
                }

                字数控制要求（严格遵守）：
                - genre: 最多10个中文字符
                - themes: 每个主题最多5个中文字符，共3个主题
                - tone: 最多8个中文字符
                - keyElements: 每个要素最多8个中文字符，共3个要素
                - recommendation: 200-300个中文字符

                内容要求：
                1. genre：准确描述书籍的类型和流派
                2. themes：提炼核心主题，言简意赅
                3. tone：概括书籍的整体基调和氛围
                4. keyElements：提取最具代表性的3个关键元素（人物、事件、概念、场景等）
                5. recommendation：包含以下内容：
                   - 书籍的核心内容和主要观点（2-3句话）
                   - 书籍的独特价值和特色（1-2句话）
                   - 适合的读者群体和阅读收获（1-2句话）

                注意事项：
                - 只返回JSON格式，不要包含任何其他文字
                - 所有字段都必须填写，不能为空
                - 严格控制每个字段的字数，避免超出限制
                - themes和keyElements必须是字符串数组
                - 确保内容准确、精炼、有价值
                """, bookTitle);
    }

    /**
     * 构建图片内容提示词（仅描述内容，不包含风格）
     * 书籍信息会被转换为详细的内容描述，风格由AIService的默认提示词提供
     *
     * 注意：会对提示词进行压缩（去除多余空格、换行），适配不同模型的长度限制
     */
    private String buildImageContentPrompt(BookAnalyse analysis) {
        // 将JSONArray转换为字符串列表
        String themesStr = analysis.getThemes() != null ?
            String.join("、", analysis.getThemes().toJavaList(String.class)) : "";
        String keyElementsStr = analysis.getKeyElements() != null ?
            String.join("、", analysis.getKeyElements().toJavaList(String.class)) : "";

        // 提取推荐语的核心内容（前80字）作为补充描述
        String shortRecommendation = "";
        if (analysis.getRecommendation() != null && analysis.getRecommendation().length() > 0) {
            shortRecommendation = analysis.getRecommendation().length() > 80 ?
                    analysis.getRecommendation().substring(0, 80) + "..." :
                    analysis.getRecommendation();
        }

        // 提示词
        String prompt = String.format("""
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
                   - 保持横向16:9布局，信息密度高但不拥挤
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

        // 压缩提示词：去除多余空格、换行、制表符
        String compressedPrompt = prompt
                .replaceAll("\\s+", " ")  // 将所有连续空白字符（空格、换行、制表符等）替换为单个空格
                .trim();                   // 去除首尾空格

        log.debug("原始提示词长度: {}, 压缩后长度: {}", prompt.length(), compressedPrompt.length());
        return compressedPrompt;
    }

    /**
     * 解析AI响应
     */
    private BookAnalyse parseAIResponse(String bookTitle, String aiResponse) {
        try {
            // 移除可能的markdown代码块标记
            String jsonStr = aiResponse.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            JSONObject json = JSON.parseObject(jsonStr);

            BookAnalyse bookAnalyse = new BookAnalyse();
            bookAnalyse.setTitle(bookTitle);
            bookAnalyse.setGenre(json.getString("genre"));
            bookAnalyse.setThemes(json.getJSONArray("themes"));
            bookAnalyse.setTone(json.getString("tone"));
            bookAnalyse.setKeyElements(json.getJSONArray("keyElements"));
            bookAnalyse.setRecommendation(json.getString("recommendation"));

            return bookAnalyse;
        } catch (Exception e) {
            log.error("解析AI响应失败，书名: {}, 响应内容: {}", bookTitle, aiResponse, e);

            // 返回一个包含原始响应的基础分析结果
            BookAnalyse fallbackAnalyse = new BookAnalyse();
            fallbackAnalyse.setTitle(bookTitle);
            fallbackAnalyse.setRecommendation("AI分析结果解析失败，原始响应: " + aiResponse.substring(0, Math.min(500, aiResponse.length())));
            return fallbackAnalyse;
        }
    }
}
