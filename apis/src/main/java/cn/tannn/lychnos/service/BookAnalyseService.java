package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.ai.service.AIService;
import cn.tannn.lychnos.dao.BookAnalyseDao;
import cn.tannn.lychnos.entity.BookAnalyse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public BookAnalyseService(AIService aiService) {
        super(BookAnalyse.class);
        this.aiService = aiService;
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
                请对书籍《%s》进行全面分析，并以JSON格式返回结果，格式如下：
                {
                  "genre": "类型/流派，如：科幻、小说、历史、哲学等",
                  "themes": ["主题1", "主题2", "主题3"],
                  "tone": "基调，如：严肃、轻松、幽默、深刻等",
                  "keyElements": ["关键要素1", "关键要素2", "关键要素3"],
                  "recommendation": "200-300字的书籍综述和推荐理由"
                }

                请确保：
                1. themes 和 keyElements 都是字符串数组
                2. recommendation 要包含书籍的核心内容、特色和推荐理由
                3. 只返回JSON，不要包含其他文字
                """, bookTitle);
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
