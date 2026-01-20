package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.ai.service.AIService;
import cn.tannn.lychnos.common.constant.BookSourceType;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.vo.BookExtractVO;
import cn.tannn.lychnos.dao.BookAnalyseDao;
import cn.tannn.lychnos.dao.UserInterestDao;
import cn.tannn.lychnos.entity.BookAnalyse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
    private final UserInterestDao userInterestDao;
    private final UserAnalysisLogService userAnalysisLogService;
    private final AIModelService aiModelService;

    public BookAnalyseService(AIService aiService, ImageStorageService imageStorageService,
                              UserInterestDao userInterestDao, UserAnalysisLogService userAnalysisLogService,
                              AIModelService aiModelService) {
        super(BookAnalyse.class);
        this.aiService = aiService;
        this.imageStorageService = imageStorageService;
        this.userInterestDao = userInterestDao;
        this.userAnalysisLogService = userAnalysisLogService;
        this.aiModelService = aiModelService;
    }

    /**
     * 从用户输入中提取书籍信息（书名和作者）
     * 优先检查数据库和用户反馈记录
     * @param userInput 用户输入的书籍信息
     * @param userId 用户ID
     * @return 书籍信息列表（书名+作者）
     */
    public List<BookExtractVO> extractBooks(String userInput, Long userId) {
        log.info("开始从用户输入提取书籍信息，用户ID: {}, 输入: {}", userId, userInput);

        // 1. 先尝试在数据库中查找（简单的书名匹配）
        String trimmedInput = userInput.trim();
        Optional<BookAnalyse> existingBook = getJpaBasicsDao().findByTitle(trimmedInput);

        if (existingBook.isPresent() && existingBook.get().getPosterUrl() != null && !existingBook.get().getPosterUrl().isEmpty()) {
            // 数据库中找到了已分析的书籍
            BookAnalyse found = existingBook.get();
            log.info("在数据库中找到已分析的书籍: {}", found.getTitle());

            // 2. 检查当前用户是否已经对这本书进行过反馈
            var userInterest = userInterestDao.findByUserIdAndBookTitle(userId, found.getTitle());

            if (userInterest.isPresent()) {
                // 用户已经反馈过这本书，标记为 ALREADY_ANALYZED
                log.info("用户已经对书籍进行过反馈: {}", found.getTitle());

                // 记录使用已有数据日志
                userAnalysisLogService.saveUseExistingDataLog(userId, getUserName(userId), null, found.getTitle(), found.getId());

                // 创建结果列表，第一本是用户已反馈的书籍
                List<BookExtractVO> result = new ArrayList<>();
                result.add(new BookExtractVO(
                    found.getTitle(),
                    found.getAuthor(),
                    true,
                    BookSourceType.ALREADY_ANALYZED
                ));

                // 尝试调用AI获取相似推荐
                try {
                    String prompt = buildExtractPromptWithFound(userInput, found.getTitle(), found.getAuthor());
                    String aiResponse = aiService.generateText(userId, prompt);

                    // 记录AI提取日志（成功）
                    userAnalysisLogService.saveExtractLog(userId, getUserName(userId), null, getTextModel(userId), userInput, null, true, null);

                    List<BookExtractVO> aiBooks = parseExtractResponse(aiResponse);

                    // 将AI推荐的书籍添加到结果中（排除已找到的书籍）
                    for (BookExtractVO book : aiBooks) {
                        if (!book.getTitle().equals(found.getTitle())) {
                            result.add(book);
                        }
                    }

                    log.info("返回{}本书籍（1本已反馈 + {}本推荐）", result.size(), result.size() - 1);
                } catch (Exception e) {
                    log.warn("AI推荐失败，仅返回数据库中的书籍，书名: {}, 错误: {}", found.getTitle(), e.getMessage());
                    // 记录AI提取日志（失败）
                    userAnalysisLogService.saveExtractLog(userId, getUserName(userId), null, getTextModel(userId), userInput, null, false, e.getMessage());
                }

                // 无论AI是否成功，都返回至少包含数据库书籍的结果
                return result;
            } else {
                // 书籍已分析但用户未反馈，尝试使用AI获取推荐，失败则返回数据库书籍
                log.info("书籍已分析但用户未反馈，尝试AI提取: {}", found.getTitle());

                try {
                    String prompt = buildExtractPrompt(userInput);
                    String aiResponse = aiService.generateText(userId, prompt);

                    // 记录AI提取日志（成功）
                    userAnalysisLogService.saveExtractLog(userId, getUserName(userId), null, getTextModel(userId), userInput, null, true, null);

                    return parseExtractResponse(aiResponse);
                } catch (Exception e) {
                    log.warn("AI提取失败，返回数据库中的书籍，书名: {}, 错误: {}", found.getTitle(), e.getMessage());

                    // 记录AI提取日志（失败）
                    userAnalysisLogService.saveExtractLog(userId, getUserName(userId), null, getTextModel(userId), userInput, null, false, e.getMessage());

                    // AI失败，返回数据库中的书籍
                    List<BookExtractVO> fallbackResult = new ArrayList<>();
                    fallbackResult.add(new BookExtractVO(
                        found.getTitle(),
                        found.getAuthor(),
                        false,
                        BookSourceType.USER_INPUT
                    ));
                    return fallbackResult;
                }
            }
        }

        // 3. 数据库中未找到，使用正常的AI提取流程（这里失败就真的失败了，因为没有备选数据）
        try {
            String prompt = buildExtractPrompt(userInput);
            String aiResponse = aiService.generateText(userId, prompt);

            // 记录AI提取日志（成功）
            userAnalysisLogService.saveExtractLog(userId, getUserName(userId), null, getTextModel(userId), userInput, null, true, null);

            // 解析AI响应
            return parseExtractResponse(aiResponse);
        } catch (Exception e) {
            // 记录AI提取日志（失败）
            userAnalysisLogService.saveExtractLog(userId, getUserName(userId), null, getTextModel(userId), userInput, null, false, e.getMessage());
            throw e;
        }
    }

    /**
     * 根据书名和作者分析书籍
     * @param bookTitle 书名
     * @param author 作者
     * @param userId 用户ID
     * @return 书籍分析
     */
    public BookAnalyse analyse(String bookTitle, String author, Long userId){
        // 先查询是否已经分析过
        Optional<BookAnalyse> existingAnalyse = getJpaBasicsDao().findByTitle(bookTitle);
        if (existingAnalyse.isPresent()) {
            BookAnalyse existing = existingAnalyse.get();

            // 如果已有图片，直接返回
            if (existing.getPosterUrl() != null && !existing.getPosterUrl().isEmpty()) {
                log.info("书籍已分析过且有图片，延时1秒后返回，书名: {}", bookTitle);

                // 记录使用已有数据日志
                userAnalysisLogService.saveUseExistingDataLog(userId, getUserName(userId), null, bookTitle, existing.getId());

                try {
                    Thread.sleep(1000); // 延时1秒，提供视觉差
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("延时被中断", e);
                }
                return existing;
            }

            // 如果没有图片，尝试生成图片
            log.info("书籍已分析过但缺少图片，尝试生成图片，书名: {}", bookTitle);
            generateAndSavePoster(existing, userId, bookTitle);
            return getJpaBasicsDao().save(existing);
        }

        // 使用AI进行分析
        log.info("开始AI分析书籍，书名: {}, 作者: {}, 用户ID: {}", bookTitle, author, userId);

        try {
            String prompt = buildAnalysisPrompt(bookTitle, author);
            String aiResponse = aiService.generateText(userId, prompt);

            // 记录AI解析日志（成功）
            userAnalysisLogService.saveParseLog(userId, getUserName(userId), null, getTextModel(userId), bookTitle, null, true, null);

            // 解析AI响应并保存
            BookAnalyse bookAnalyse = parseAIResponse(bookTitle, author, aiResponse);

            // 生成书籍分析信息图
            log.info("开始生成书籍分析信息图，书名: {}", bookTitle);
            generateAndSavePoster(bookAnalyse, userId, bookTitle);

            BookAnalyse saved = getJpaBasicsDao().save(bookAnalyse);

            log.info("书籍分析完成并保存，书名: {}", bookTitle);
            return saved;
        } catch (Exception e) {
            // 记录AI解析日志（失败）
            userAnalysisLogService.saveParseLog(userId, getUserName(userId), null, getTextModel(userId), bookTitle, null, false, e.getMessage());
            throw e;
        }
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
     * 为书籍分析生成并保存海报图片
     * @param bookAnalyse 书籍分析对象
     * @param userId 用户ID
     * @param bookTitle 书名
     */
    private void generateAndSavePoster(BookAnalyse bookAnalyse, Long userId, String bookTitle) {
        try {
            String imageContentPrompt = buildImageContentPrompt(bookAnalyse);
            try (InputStream imageStream = aiService.generateImageStreamWithContent(userId, imageContentPrompt)) {
                if (imageStream == null) {
                    log.warn("AI 返回的图片流为 null，书名: {}", bookTitle);
                    // 记录生图日志（失败）
                    userAnalysisLogService.saveImageLog(userId, getUserName(userId), null, getImageModel(userId), bookTitle, bookAnalyse.getId(), false, "AI返回的图片流为null");
                    return;
                }
                String posterUrl = imageStorageService.saveImage(imageStream, bookTitle);
                bookAnalyse.setPosterUrl(posterUrl);
                log.info("书籍分析信息图生成并保存成功，posterUrl: {}", posterUrl);

                // 记录生图日志（成功）
                userAnalysisLogService.saveImageLog(userId, getUserName(userId), null, getImageModel(userId), bookTitle, bookAnalyse.getId(), true, null);
            }
        } catch (Exception e) {
            log.warn("书籍分析信息图生成失败，书名: {}, 错误: {}", bookTitle, e.getMessage());
            // 记录生图日志（失败）
            userAnalysisLogService.saveImageLog(userId, getUserName(userId), null, getImageModel(userId), bookTitle, bookAnalyse.getId(), false, e.getMessage());
            // 图片生成失败不影响业务流程，posterUrl 保持为 null，后续可以补充生成
        }
    }

    /**
     * 构建书籍提取提示词-包含相似书籍推荐
     */
    private String buildExtractPrompt(String userInput) {
        return String.format("""
                你是一位专业的图书信息识别专家和推荐专家。请从用户输入中提取书籍信息，并推荐相似书籍。

                用户输入：%s

                任务要求：
                1. 识别用户输入的书籍（如果能识别到）
                2. 推荐与用户输入相关的4-5本真实存在的书籍（类型相似、主题相关、作者相关等）
                3. 如果用户输入无法识别为书籍，则根据输入内容推荐5本相关的真实书籍

                返回JSON格式（数组，总共返回5本书）：
                [
                  {
                    "title": "书名",
                    "author": "作者",
                    "sourceType": "类型标识",
                    "sourceLabel": "来源说明"
                  }
                ]

                sourceType 类型说明：
                - "USER_INPUT": 用户输入的书籍（AI识别出的）
                - "SIMILAR": 相似推荐书籍（与用户输入相关）
                - "NOT_FOUND_RECOMMEND": 未找到时的推荐书籍（用户输入无法识别为书籍时）

                sourceLabel 说明示例：
                - "您输入的书籍"
                - "相似推荐：同作者作品"
                - "相似推荐：同类型书籍"
                - "相似推荐：相关主题"
                - "您可能在找这本书"

                识别和推荐规则：
                1. 如果用户明确提供了书名和作者，将其作为第一本书（sourceType="USER_INPUT"）
                2. 如果只提供了书名，尝试根据你的知识库补充作者信息，作为第一本书（sourceType="USER_INPUT"）
                3. 如果用户输入无法识别为书籍，则全部返回推荐书籍（sourceType="NOT_FOUND_RECOMMEND"）
                4. 在识别出用户输入的书籍后，推荐4本相似书籍（sourceType="SIMILAR"）
                5. 推荐的书籍必须是真实存在的，不能编造虚假书籍
                6. 推荐书籍应该与用户输入高度相关（同作者、同类型、同主题等）
                7. 如果用户输入包含多本书，只提取第一本作为USER_INPUT，其余作为SIMILAR推荐

                示例1（能识别到书籍）：
                输入："三体"
                返回：
                [
                  {"title": "三体", "author": "刘慈欣", "sourceType": "USER_INPUT", "sourceLabel": "您输入的书籍"},
                  {"title": "三体Ⅱ·黑暗森林", "author": "刘慈欣", "sourceType": "SIMILAR", "sourceLabel": "相似推荐：系列作品"},
                  {"title": "三体Ⅲ·死神永生", "author": "刘慈欣", "sourceType": "SIMILAR", "sourceLabel": "相似推荐：系列作品"},
                  {"title": "球状闪电", "author": "刘慈欣", "sourceType": "SIMILAR", "sourceLabel": "相似推荐：同作者作品"},
                  {"title": "流浪地球", "author": "刘慈欣", "sourceType": "SIMILAR", "sourceLabel": "相似推荐：同作者作品"}
                ]

                示例2（无法识别为书籍）：
                输入："科幻小说"
                返回：
                [
                  {"title": "三体", "author": "刘慈欣", "sourceType": "NOT_FOUND_RECOMMEND", "sourceLabel": "您可能在找这本书"},
                  {"title": "银河帝国：基地", "author": "艾萨克·阿西莫夫", "sourceType": "NOT_FOUND_RECOMMEND", "sourceLabel": "您可能在找这本书"},
                  {"title": "沙丘", "author": "弗兰克·赫伯特", "sourceType": "NOT_FOUND_RECOMMEND", "sourceLabel": "您可能在找这本书"},
                  {"title": "神经漫游者", "author": "威廉·吉布森", "sourceType": "NOT_FOUND_RECOMMEND", "sourceLabel": "您可能在找这本书"},
                  {"title": "安德的游戏", "author": "奥森·斯科特·卡德", "sourceType": "NOT_FOUND_RECOMMEND", "sourceLabel": "您可能在找这本书"}
                ]

                注意事项：
                - 只返回JSON格式，不要包含任何其他文字
                - 确保JSON格式正确，可以被解析
                - 总是返回5本书（1本用户输入+4本推荐，或5本推荐）
                - 所有推荐的书籍必须是真实存在的，不能编造
                - sourceLabel要简洁明了，帮助用户理解书籍来源
                """, userInput);
    }

    /**
     * 构建书籍提取提示词（已找到书籍的情况）
     * 用于数据库中已找到用户输入的书籍，只需要AI推荐相似书籍
     */
    private String buildExtractPromptWithFound(String userInput, String foundTitle, String foundAuthor) {
        return String.format("""
                你是一位专业的图书推荐专家。用户输入了"%s"，我们已经在数据库中找到了这本书：《%s》（作者：%s）。

                现在请你推荐4-5本与这本书相似的真实存在的书籍。

                返回JSON格式（数组，只返回推荐书籍，不要包含已找到的书籍）：
                [
                  {
                    "title": "书名",
                    "author": "作者",
                    "sourceType": "SIMILAR",
                    "sourceLabel": "来源说明"
                  }
                ]

                推荐规则：
                1. 推荐4-5本与《%s》相似的书籍
                2. 相似维度可以是：同作者作品、系列作品、同类型书籍、相关主题等
                3. 所有推荐书籍的sourceType都必须是"SIMILAR"
                4. sourceLabel要说明推荐理由，如："相似推荐：同作者作品"、"相似推荐：系列作品"、"相似推荐：同类型书籍"等
                5. 推荐的书籍必须是真实存在的，不能编造虚假书籍
                6. 不要在返回结果中包含已找到的书籍《%s》

                示例：
                已找到书籍：《三体》（刘慈欣）
                返回：
                [
                  {"title": "三体Ⅱ·黑暗森林", "author": "刘慈欣", "sourceType": "SIMILAR", "sourceLabel": "相似推荐：系列作品"},
                  {"title": "三体Ⅲ·死神永生", "author": "刘慈欣", "sourceType": "SIMILAR", "sourceLabel": "相似推荐：系列作品"},
                  {"title": "球状闪电", "author": "刘慈欣", "sourceType": "SIMILAR", "sourceLabel": "相似推荐：同作者作品"},
                  {"title": "流浪地球", "author": "刘慈欣", "sourceType": "SIMILAR", "sourceLabel": "相似推荐：同作者作品"}
                ]

                注意事项：
                - 只返回JSON格式，不要包含任何其他文字
                - 确保JSON格式正确，可以被解析
                - 返回4-5本推荐书籍
                - 所有书籍必须是真实存在的
                - 不要包含已找到的书籍
                """, userInput, foundTitle, foundAuthor, foundTitle, foundTitle);
    }

    /**
     * 构建分析提示词
     */
    private String buildAnalysisPrompt(String bookTitle, String author) {
        String bookInfo = author != null && !author.trim().isEmpty()
            ? String.format("《%s》（作者：%s）", bookTitle, author)
            : String.format("《%s》", bookTitle);

        return String.format("""
                你是一位资深的图书评论专家和文学研究者。请对书籍%s进行全面而精准的分析。

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
                """, bookInfo);
    }

    /**
     * 构建图片内容提示词（仅描述内容，不包含风格）
     * 书籍信息会被转换为详细的内容描述，风格由AIService的默认提示词提供
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

        return prompt;
    }

    /**
     * 解析书籍提取响应（增强版：包含来源标注）
     */
    private List<BookExtractVO> parseExtractResponse(String aiResponse) {
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

            JSONArray jsonArray = JSON.parseArray(jsonStr);
            List<BookExtractVO> result = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                String title = json.getString("title");
                String author = json.getString("author");
                String sourceTypeStr = json.getString("sourceType");

                if (title != null && !title.isEmpty()) {
                    // 将字符串转换为枚举类型
                    BookSourceType sourceType = BookSourceType.fromCode(sourceTypeStr);

                    result.add(new BookExtractVO(title, author, false, sourceType));
                }
            }

            log.info("成功提取{}本书籍信息（包含推荐）", result.size());
            return result;
        } catch (Exception e) {
            log.error("解析书籍提取响应失败，响应内容: {}", aiResponse, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取用户名（从JWT中获取，避免数据库查询）
     */
    private String getUserName(Long userId) {
        try {
            var attributes = RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            var request = ((ServletRequestAttributes) attributes).getRequest();
            var jwtInfo = UserUtil.getLoginJwtExtendInfoExpires(request);
            String userName = jwtInfo.getUserName();
            return userName != null ? userName : jwtInfo.getLoginName();
        } catch (Exception e) {
            log.warn("从JWT获取用户名失败，userId: {}", userId, e);
            return null;
        }
    }

    private cn.tannn.lychnos.entity.AIModel getTextModel(Long userId) {
        try {
            var models = aiModelService.findByUserIdAndType(userId, cn.tannn.lychnos.common.constant.ModelType.TEXT);
            return models.stream().filter(m -> Boolean.TRUE.equals(m.getEnabled())).findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private cn.tannn.lychnos.entity.AIModel getImageModel(Long userId) {
        try {
            var models = aiModelService.findByUserIdAndType(userId, cn.tannn.lychnos.common.constant.ModelType.IMAGE);
            return models.stream().filter(m -> Boolean.TRUE.equals(m.getEnabled())).findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析AI响应
     */
    private BookAnalyse parseAIResponse(String bookTitle, String author, String aiResponse) {
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
            bookAnalyse.setAuthor(author);
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
            fallbackAnalyse.setAuthor(author);
            fallbackAnalyse.setRecommendation("AI分析结果解析失败，原始响应: " + aiResponse.substring(0, Math.min(500, aiResponse.length())));
            return fallbackAnalyse;
        }
    }
}
