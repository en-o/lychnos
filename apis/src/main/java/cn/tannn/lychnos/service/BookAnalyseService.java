package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.jpa.service.J2ServiceImpl;
import cn.tannn.lychnos.ai.prompt.BookPrompt;
import cn.tannn.lychnos.ai.prompt.ImagePrompt;
import cn.tannn.lychnos.ai.service.AIService;
import cn.tannn.lychnos.common.constant.BookSourceType;
import cn.tannn.lychnos.common.constant.ModelType;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.vo.BookExtractVO;
import cn.tannn.lychnos.dao.BookAnalyseDao;
import cn.tannn.lychnos.dao.UserInterestDao;
import cn.tannn.lychnos.entity.AIModel;
import cn.tannn.lychnos.entity.BookAnalyse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
                userAnalysisLogService.saveUseExistingDataLog(userId, UserUtil.userRequestInfo(), found.getTitle(), found.getId());

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
                    String userMessage = buildExtractUserMessageWithFound(found.getTitle(), found.getAuthor());
                    String aiResponse = aiService.generateTextWithSystem(userId, BookPrompt.EXTRACT_EXPERT, userMessage);

                    // 记录AI提取日志（成功）
                    userAnalysisLogService.saveExtractLog(userId, UserUtil.userRequestInfo(), getTextModel(userId), userInput, null, true, null);

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
                    userAnalysisLogService.saveExtractLog(userId, UserUtil.userRequestInfo(), getTextModel(userId), userInput, null, false, e.getMessage());
                }

                // 无论AI是否成功，都返回至少包含数据库书籍的结果
                return result;
            } else {
                // 书籍已分析但用户未反馈，尝试使用AI获取推荐，失败则返回数据库书籍
                log.info("书籍已分析但用户未反馈，尝试AI提取: {}", found.getTitle());

                try {
                    String userMessage = buildExtractUserMessage(userInput);
                    String aiResponse = aiService.generateTextWithSystem(userId, BookPrompt.EXTRACT_EXPERT, userMessage);

                    // 记录AI提取日志（成功）
                    userAnalysisLogService.saveExtractLog(userId, UserUtil.userRequestInfo(), getTextModel(userId), userInput, null, true, null);

                    return parseExtractResponse(aiResponse);
                } catch (Exception e) {
                    log.warn("AI提取失败，返回数据库中的书籍，书名: {}, 错误: {}", found.getTitle(), e.getMessage());

                    // 记录AI提取日志（失败）
                    userAnalysisLogService.saveExtractLog(userId, UserUtil.userRequestInfo(), getTextModel(userId), userInput, null, false, e.getMessage());

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
            String userMessage = buildExtractUserMessage(userInput);
            String aiResponse = aiService.generateTextWithSystem(userId, BookPrompt.EXTRACT_EXPERT, userMessage);

            // 记录AI提取日志（成功）
            userAnalysisLogService.saveExtractLog(userId, UserUtil.userRequestInfo(), getTextModel(userId), userInput, null, true, null);

            // 解析AI响应
            return parseExtractResponse(aiResponse);
        } catch (Exception e) {
            // 记录AI提取日志（失败）
            userAnalysisLogService.saveExtractLog(userId, UserUtil.userRequestInfo(), getTextModel(userId), userInput, null, false, e.getMessage());
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
                userAnalysisLogService.saveUseExistingDataLog(userId, UserUtil.userRequestInfo(), bookTitle, existing.getId());

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
            String userMessage = buildAnalysisUserMessage(bookTitle, author);
            String aiResponse = aiService.generateTextWithSystem(userId, BookPrompt.ANALYSIS_EXPERT, userMessage);

            // 记录AI解析日志（成功）
            userAnalysisLogService.saveParseLog(userId, UserUtil.userRequestInfo(), getTextModel(userId), bookTitle, null, true, null);

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
            userAnalysisLogService.saveParseLog(userId, UserUtil.userRequestInfo(), getTextModel(userId), bookTitle, null, false, e.getMessage());
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
        var userInfo = UserUtil.userRequestInfo();
        try {
            String imageContentPrompt = ImagePrompt.buildBookInfographicPrompt(bookAnalyse);
            try (InputStream imageStream = aiService.generateImageStreamWithContent(userId, imageContentPrompt)) {
                if (imageStream == null) {
                    log.warn("AI 返回的图片流为 null，书名: {}", bookTitle);
                    userAnalysisLogService.saveImageLog(userId, userInfo, getImageModel(userId), bookTitle, bookAnalyse.getId(), false, "AI返回的图片流为null");
                    return;
                }
                String posterUrl = imageStorageService.saveImage(imageStream, bookTitle);
                bookAnalyse.setPosterUrl(posterUrl);
                log.info("书籍分析信息图生成并保存成功，posterUrl: {}", posterUrl);

                userAnalysisLogService.saveImageLog(userId, userInfo, getImageModel(userId), bookTitle, bookAnalyse.getId(), true, null);
            }
        } catch (Exception e) {
            log.warn("书籍分析信息图生成失败，书名: {}, 错误: {}", bookTitle, e.getMessage());
            userAnalysisLogService.saveImageLog(userId, userInfo, getImageModel(userId), bookTitle, bookAnalyse.getId(), false, e.getMessage());
        }
    }

    /**
     * 构建书籍提取用户消息
     */
    private String buildExtractUserMessage(String userInput) {
        return String.format("用户输入：%s\n\n请根据系统提示词的要求，从用户输入中提取书籍信息并推荐相似书籍。", userInput);
    }

    /**
     * 构建书籍提取用户消息（已找到书籍的情况）
     */
    private String buildExtractUserMessageWithFound(String foundTitle, String foundAuthor) {
        return String.format("""
                已找到书籍：《%s》（作者：%s）

                请推荐4-5本与这本书相似的真实存在的书籍。
                """, foundTitle, foundAuthor);
    }

    /**
     * 构建书籍分析用户消息
     */
    private String buildAnalysisUserMessage(String bookTitle, String author) {
        String bookInfo = author != null && !author.trim().isEmpty()
            ? String.format("《%s》（作者：%s）", bookTitle, author)
            : String.format("《%s》", bookTitle);

        return String.format("请对书籍%s进行分析，返回JSON格式的分析结果。", bookInfo);
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
     * 获取文本模型（支持官方模型回退）
     * 使用 AIModelService.getEnabledModel 确保与 AIService 的模型选择逻辑一致
     */
    private AIModel getTextModel(Long userId) {
        return aiModelService.getEnabledModel(userId, ModelType.TEXT);
    }

    /**
     * 获取图片模型（支持官方模型回退）
     * 使用 AIModelService.getEnabledModel 确保与 AIService 的模型选择逻辑一致
     */
    private AIModel getImageModel(Long userId) {
        return aiModelService.getEnabledModel(userId, ModelType.IMAGE);
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
