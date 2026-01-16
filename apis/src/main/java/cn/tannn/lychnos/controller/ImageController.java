package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.mapping.PathRestController;
import cn.tannn.lychnos.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(
            summary = "获取图片",
            description = "根据 posterUrl 获取图片流\n\n" +
                    "格式：协议:鉴权:路径\n" +
                    "示例：\n" +
                    "- l:1:/20240115/三体.png （本地存储）\n" +
                    "- ali:1:/bucket/books/三体.png （阿里云OSS）\n" +
                    "- h:0:https://... （无鉴权HTTP，不应通过此接口访问）"
    )
    @GetMapping
    public void getImage(
            @Parameter(description = "posterUrl 格式（协议:鉴权:路径）", example = "l:1:/20240115/三体.png")
            @RequestParam String path,
            HttpServletResponse response) throws Exception {

        log.info("获取图片，posterUrl: {}", path);

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
