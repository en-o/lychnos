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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    @GetMapping("text")
    public ResultVO<String> testText(
            @Parameter(description = "提示词") @RequestParam("prompt") String prompt,
            HttpServletRequest request) {

        Long userId = UserUtil.userId2(request);
        log.info("AI文本生成测试，userId: {}, prompt: {}", userId, prompt);

        String result = aiService.generateText(userId, prompt);
        return ResultVO.success(result);
    }

    @Operation(summary = "文本生成测试（指定模型）", description = "使用指定的模型ID生成文本内容")
    @GetMapping("text/model")
    public ResultVO<String> testTextWithModel(
            @Parameter(description = "模型ID") @RequestParam("modelId") Long modelId,
            @Parameter(description = "提示词") @RequestParam("prompt") String prompt,
            HttpServletRequest request) {

        Long userId = UserUtil.userId2(request);
        log.info("AI文本生成测试（指定模型），userId: {}, modelId: {}, prompt: {}", userId, modelId, prompt);

        String result = aiService.generateTextWithModel(modelId, userId, prompt);
        return ResultVO.success(result);
    }

    @Operation(summary = "图片生成测试", description = "使用用户配置的默认图片模型生成图片")
    @PostMapping("image")
    public ResultVO<ImageResponse> testImage(
            @Valid @RequestBody AIPromptDTO dto,
            HttpServletRequest request) {

        Long userId = UserUtil.userId2(request);
        log.info("AI图片生成测试，userId: {}, prompt: {}", userId, dto.getPrompt());

        ImageResponse result = aiService.generateImage(userId, dto.getPrompt());
        return ResultVO.success(result);
    }

    @Operation(summary = "图片生成测试（指定模型）", description = "使用指定的模型ID生成图片")
    @PostMapping("image/model")
    public ResultVO<ImageResponse> testImageWithModel(
            @Valid @RequestBody AIPromptWithModelDTO dto,
            HttpServletRequest request) {

        Long userId = UserUtil.userId2(request);
        log.info("AI图片生成测试（指定模型），userId: {}, modelId: {}, prompt: {}", userId, dto.getModelId(), dto.getPrompt());

        ImageResponse result = aiService.generateImageWithModel(dto.getModelId(), userId, dto.getPrompt());
        return ResultVO.success(result);
    }
}
