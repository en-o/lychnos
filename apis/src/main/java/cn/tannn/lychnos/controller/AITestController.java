package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.jdevelops.result.response.ResultVO;
import cn.tannn.lychnos.ai.service.AIService;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.dto.AIPromptDTO;
import cn.tannn.lychnos.controller.dto.AIPromptWithModelDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;

/**
 * AI 测试接口
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
@PathRestController("ai/test")
@Tag(name = "AI测试", description = "用于测试 AI 模型调用功能")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class AITestController {

    private final AIService aiService;

    @Operation(summary = "文本生成测试", description = "使用用户配置的默认文本模型生成内容")
    @PostMapping("text")
    public ResultVO<String> testText(
            @Valid @RequestBody AIPromptDTO dto,
            HttpServletRequest request) {

        Long userId = UserUtil.userId2(request);
        log.info("AI文本生成测试，userId: {}, prompt: {}", userId, dto.getPrompt());

        String result = aiService.generateText(userId, dto.getPrompt());
        return ResultVO.success(result);
    }

    @Operation(summary = "文本生成测试（指定模型）", description = "使用指定的模型ID生成文本内容")
    @PostMapping("text/model")
    public ResultVO<String> testTextWithModel(
            @Valid @RequestBody AIPromptWithModelDTO dto,
            HttpServletRequest request) {

        Long userId = UserUtil.userId2(request);
        log.info("AI文本生成测试（指定模型），userId: {}, modelId: {}, prompt: {}", userId, dto.getModelId(), dto.getPrompt());

        String result = aiService.generateTextWithModel(dto.getModelId(), userId, dto.getPrompt());
        return ResultVO.success(result);
    }

    @Operation(summary = "图片生成测试（流）", description = "使用用户配置的默认图片模型生成图片，返回图片流")
    @PostMapping("image")
    public void testImage(
            @Valid @RequestBody AIPromptDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Long userId = UserUtil.userId2(request);
        log.info("AI图片生成测试，userId: {}, prompt: {}", userId, dto.getPrompt());

        try (InputStream inputStream = aiService.generateImageStream(userId, dto.getPrompt())) {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(inputStream, response.getOutputStream());
        }
    }

    @Operation(summary = "图片生成测试（指定模型，流）", description = "使用指定的模型ID生成图片，返回图片流")
    @PostMapping("image/model")
    public void testImageWithModel(
            @Valid @RequestBody AIPromptWithModelDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Long userId = UserUtil.userId2(request);
        log.info("AI图片生成测试（指定模型），userId: {}, modelId: {}, prompt: {}", userId, dto.getModelId(), dto.getPrompt());

        try (InputStream inputStream = aiService.generateImageStreamWithModel(dto.getModelId(), userId, dto.getPrompt())) {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(inputStream, response.getOutputStream());
        }
    }

    @Operation(summary = "书籍封面生成测试", description = "根据书籍信息生成封面图片（现代信息图解风格，1024x576尺寸）")
    @GetMapping("image/book-cover")
    public void testBookCoverImage(
            @Parameter(description = "书名") @RequestParam String title,
            @Parameter(description = "类型/流派") @RequestParam String genre,
            @Parameter(description = "基调") @RequestParam String tone,
            @Parameter(description = "主题（逗号分隔）") @RequestParam String themes,
            @Parameter(description = "关键要素（逗号分隔）") @RequestParam String keyElements,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Long userId = UserUtil.userId2(request);
        log.info("书籍封面生成测试，userId: {}, title: {}", userId, title);

        String contentPrompt = buildBookCoverContentPrompt(title, genre, tone, themes, keyElements);
        log.info("生成的内容提示词: {}", contentPrompt);

        try (InputStream inputStream = aiService.generateImageStreamWithContent(userId, contentPrompt)) {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(inputStream, response.getOutputStream());
        }
    }

    /**
     * 构建书籍封面内容提示词（不包含风格，风格由AIService默认提供）
     * 精简版，控制字数防止提示词溢出
     */
    private String buildBookCoverContentPrompt(String title, String genre, String tone, String themes, String keyElements) {
        return String.format("""
                Book: "%s"
                Genre: %s | Tone: %s
                Themes: %s
                Key Elements: %s

                Create an infographic poster featuring:
                - Book title in Chinese (prominent display)
                - Genre and tone in organized sections
                - Visual symbols for themes
                - Icons/illustrations for key elements
                - Clean, educational layout
                """,
                title,
                genre,
                tone,
                themes,
                keyElements
        );
    }
}
