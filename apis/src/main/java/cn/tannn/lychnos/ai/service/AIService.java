package cn.tannn.lychnos.ai.service;

import org.springframework.ai.image.ImageResponse;

import java.io.InputStream;

/**
 * AI 服务接口
 * 提供统一的 AI 调用能力
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
public interface AIService {

    /**
     * 文本生成
     *
     * @param userId 用户ID
     * @param prompt 提示词
     * @return 生成的文本
     */
    String generateText(Long userId, String prompt);

    /**
     * 文本生成（使用指定模型）
     *
     * @param modelId 模型ID
     * @param userId  用户ID
     * @param prompt  提示词
     * @return 生成的文本
     */
    String generateTextWithModel(Long modelId, Long userId, String prompt);

    /**
     * 图片生成
     *
     * @param userId 用户ID
     * @param prompt 提示词
     * @return 图片响应
     */
    ImageResponse generateImage(Long userId, String prompt);

    /**
     * 图片生成（使用指定模型）
     *
     * @param modelId 模型ID
     * @param userId  用户ID
     * @param prompt  提示词
     * @return 图片响应
     */
    ImageResponse generateImageWithModel(Long modelId, Long userId, String prompt);

    /**
     * 图片生成（返回图片流）
     *
     * @param userId 用户ID
     * @param prompt 提示词
     * @return 图片输入流
     */
    InputStream generateImageStream(Long userId, String prompt);

    /**
     * 图片生成（使用指定模型，返回图片流）
     *
     * @param modelId 模型ID
     * @param userId  用户ID
     * @param prompt  提示词
     * @return 图片输入流
     */
    InputStream generateImageStreamWithModel(Long modelId, Long userId, String prompt);
}
