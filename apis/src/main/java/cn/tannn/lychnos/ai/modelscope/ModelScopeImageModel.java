package cn.tannn.lychnos.ai.modelscope;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
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
            ImageGeneration imageGeneration = new ImageGeneration(imageUrl);
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
        String url = baseUrl + "/v1/images/generations";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("X-ModelScope-Async-Mode", "true");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        log.debug("提交异步任务到 ModelScope: {}", url);
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
        if (responseBody == null || !responseBody.containsKey("request_id")) {
            throw new RuntimeException("响应中缺少 request_id");
        }

        String taskId = (String) responseBody.get("request_id");
        log.info("异步任务已提交，taskId: {}", taskId);
        return taskId;
    }

    /**
     * 轮询任务结果
     */
    private String pollTaskResult(String taskId) throws InterruptedException {
        String url = baseUrl + "/v1/async-result/" + taskId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
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
                    String status = (String) result.get("status");

                    if ("SUCCEEDED".equals(status)) {
                        // 任务成功，提取图片 URL
                        Map<String, Object> output = (Map<String, Object>) result.get("output");
                        if (output != null && output.containsKey("image_url")) {
                            String imageUrl = (String) output.get("image_url");
                            log.info("图片生成成功，imageUrl: {}", imageUrl);
                            return imageUrl;
                        }
                    } else if ("FAILED".equals(status)) {
                        String error = result.getOrDefault("error", "Unknown error").toString();
                        throw new RuntimeException("任务失败: " + error);
                    }
                    // PENDING 或 RUNNING 状态，继续轮询
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
