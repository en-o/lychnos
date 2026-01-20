package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.lychnos.common.util.SignedUrlUtil;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;

/**
 * 图片访问接口
 * <p>
 * 支持统一格式：协议:鉴权:路径
 * - 协议: h(HTTP/HTTPS) / ali(阿里云OSS) / qiniu(七牛云) / s3(AWS S3) / f(FTP) / l(本地)
 * - 鉴权: 0(无需鉴权，不应通过此接口访问) / 1(需要鉴权，通过此接口代理访问)
 * <p>
 * 访问示例：GET /api/image?path=l:1:/20240115/三体.png
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/17
 */
@PathRestController("image")
@Tag(name = "图片访问", description = "图片资源访问接口（支持协议:鉴权:路径格式）")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageStorageService imageStorageService;

    @Value("${app.security.aes-secret-key}")
    private String secretKey;

    @Operation(
            summary = "获取图片",
            description = "根据 posterUrl 获取图片流\n\n" +
                    "格式：协议:鉴权:路径\n" +
                    "示例：\n" +
                    "- l:1:/20240115/三体.png （本地存储）\n" +
                    "- ali:1:/bucket/books/三体.png （阿里云OSS）\n" +
                    "- h:0:https://... （无鉴权HTTP，不应通过此接口访问）\n\n" +
                    "未登录用户访问需要提供签名参数：expires 和 signature"
    )
    @ApiMapping(checkToken = false, value = "", method = RequestMethod.GET)
    public void getImage(
            @Parameter(description = "posterUrl 格式（协议:鉴权:路径）", example = "l:1:/20240115/三体.png")
            @RequestParam String path,
            @Parameter(description = "签名过期时间戳（秒）", example = "1737360000")
            @RequestParam(required = false) Long expires,
            @Parameter(description = "URL 签名", example = "abc123...")
            @RequestParam(required = false) String signature,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        log.info("获取图片，posterUrl: {}", path);

        // 检查用户是否已登录
        Long userId = null;
        try {
            userId = UserUtil.userId2(request);
        } catch (Exception e) {
            log.debug("获取用户ID失败，可能是未登录用户: {}", e.getMessage());
        }

        // 未登录用户需要验证签名
        if (userId == null) {
            if (expires == null || signature == null) {
                log.warn("未登录用户访问图片缺少签名参数，path: {}", path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Missing signature");
                return;
            }

            // 验证签名
            boolean isValid = SignedUrlUtil.verifySignature(path, expires, signature, secretKey);
            if (!isValid) {
                log.warn("签名验证失败，path: {}, expires: {}", path, expires);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Invalid or expired signature");
                return;
            }

            log.info("未登录用户签名验证成功，path: {}", path);
        }

        // 获取并返回图片
        try (InputStream inputStream = imageStorageService.getImage(path)) {
            if (inputStream == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(inputStream, response.getOutputStream());
        }
    }
}
