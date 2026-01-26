package cn.tannn.lychnos.ai.modelscope;

import cn.tannn.lychnos.common.util.ZipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ModelScope 图片生成模型（异步调用）
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/16
 */
@Slf4j
@RequiredArgsConstructor
public class ModelScopeImageModel implements ImageModel {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final RestTemplate restTemplate;

    /**
     * ModelScope 提示词最大长度限制
     */
    private static final int MAX_PROMPT_LENGTH = 2000;

    /**
     * 最大轮询次数
     */
    private static final int MAX_POLL_ATTEMPTS = 60;

    /**
     * 轮询间隔（毫秒）
     */
    private static final long POLL_INTERVAL_MS = 2000;

    @Override
    public ImageResponse call(ImagePrompt imagePrompt) {
        try {
            log.info("调用 ModelScope 异步图片生成，model: {}, prompt: {}",
                    model, imagePrompt.getInstructions().get(0).getText());

            // 1. 提交异步任务
            String taskId = submitAsyncTask(imagePrompt.getInstructions().get(0).getText());

            // 2. 轮询获取结果
            String imageUrl = pollTaskResult(taskId);

            // 3. 构建响应
            Image image = new Image(imageUrl, null);
            ImageGeneration imageGeneration = new ImageGeneration(image);
            return new ImageResponse(Collections.singletonList(imageGeneration));

        } catch (Exception e) {
            log.error("ModelScope 图片生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("ModelScope 图片生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提交异步任务
     */
    private String submitAsyncTask(String prompt) {
        // ModelScope 限制提示词长度不超过2000字符，使用智能压缩策略
        String finalPrompt = prompt;
        if (prompt.length() > MAX_PROMPT_LENGTH) {
            log.info("提示词长度 {} 超过限制 {}，尝试智能压缩", prompt.length(), MAX_PROMPT_LENGTH);

            // 第一步：尝试普通压缩
            finalPrompt = ZipUtil.smartCompressPrompt(prompt, MAX_PROMPT_LENGTH);

            // 第二步：如果还是超长，使用激进压缩
            if (finalPrompt.length() > MAX_PROMPT_LENGTH) {
                log.warn("普通压缩后长度 {} 仍超限，使用激进压缩", finalPrompt.length());
                finalPrompt = ZipUtil.compressPromptAggressive(prompt);

                // 第三步：最后的保险，如果还是超长才截断
                if (finalPrompt.length() > MAX_PROMPT_LENGTH) {
                    log.error("激进压缩后长度 {} 仍超限，被迫截断到 {}",
                             finalPrompt.length(), MAX_PROMPT_LENGTH);
                    finalPrompt = finalPrompt.substring(0, MAX_PROMPT_LENGTH);
                }
            }

            log.info("提示词压缩完成：{} -> {} 字符（节省 {}%）",
                    prompt.length(),
                    finalPrompt.length(),
                    String.format("%.1f", (1.0 - (double)finalPrompt.length() / prompt.length()) * 100));
        }

        String url = baseUrl + "/v1/images/generations";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("X-ModelScope-Async-Mode", "true");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", finalPrompt);
        requestBody.put("size", "1920x1080");  // 设置图片尺寸为 1920x1080 (Full HD, 16:9横向)

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        log.debug("提交异步任务到 ModelScope，提示词长度: {}, 尺寸: 1920x1080", finalPrompt.length());
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("提交异步任务失败: " + response.getStatusCode());
        }

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("task_id")) {
            throw new RuntimeException("响应中缺少 task_id");
        }

        String taskId = (String) responseBody.get("task_id");
        log.info("异步任务已提交，taskId: {}", taskId);
        return taskId;
    }

    /**
     * 轮询任务结果
     */
    private String pollTaskResult(String taskId) throws InterruptedException {
        String url = baseUrl + "/v1/tasks/" + taskId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.set("X-ModelScope-Task-Type", "image_generation");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        for (int i = 0; i < MAX_POLL_ATTEMPTS; i++) {
            log.debug("轮询任务结果，taskId: {}, 第 {} 次尝试", taskId, i + 1);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        Map.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> result = response.getBody();
                    String status = (String) result.get("task_status");

                    if ("SUCCEEDED".equals(status) || "SUCCEED".equals(status)) {
                        // 任务成功，提取图片 URL

                        // 1. 尝试从 output_images 数组获取（魔搭社区实际返回格式）
                        if (result.containsKey("output_images")) {
                            Object outputImagesObj = result.get("output_images");
                            if (outputImagesObj instanceof List<?> outputImages) {
                                if (!outputImages.isEmpty() && outputImages.get(0) instanceof String imageUrl) {
                                    log.info("图片生成成功，从output_images获取URL: {}", imageUrl);
                                    return imageUrl;
                                }
                            }
                        }

                        // 2. 尝试从 output 对象获取（备用）
                        Object outputObj = result.get("output");
                        if (outputObj instanceof Map) {
                            Map<String, Object> output = (Map<String, Object>) outputObj;
                            if (output.containsKey("image_url")) {
                                String imageUrl = (String) output.get("image_url");
                                log.info("图片生成成功，从output.image_url获取URL: {}", imageUrl);
                                return imageUrl;
                            } else if (output.containsKey("url")) {
                                String imageUrl = (String) output.get("url");
                                log.info("图片生成成功，从output.url获取URL: {}", imageUrl);
                                return imageUrl;
                            }
                        } else if (outputObj instanceof String imageUrl) {
                            // output可能直接是URL字符串
                            log.info("图片生成成功，从output获取URL: {}", imageUrl);
                            return imageUrl;
                        }

                        log.error("无法从响应中提取图片URL，完整响应: {}", result);
                        throw new RuntimeException("无法从响应中提取图片URL");

                    } else if ("FAILED".equals(status)) {
                        String error = result.getOrDefault("error", "Unknown error").toString();
                        throw new RuntimeException("任务失败: " + error);
                    }
                    // PENDING 或 RUNNING 状态，继续轮询
                    log.debug("任务状态: {}, 继续轮询", status);
                }
            } catch (Exception e) {
                log.warn("轮询任务结果异常: {}", e.getMessage());
            }

            // 等待后重试
            TimeUnit.MILLISECONDS.sleep(POLL_INTERVAL_MS);
        }

        throw new RuntimeException("任务超时，已达最大轮询次数: " + MAX_POLL_ATTEMPTS);
    }
}
