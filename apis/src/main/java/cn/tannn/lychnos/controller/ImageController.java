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
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/17
 */
@PathRestController("image")
@Tag(name = "图片访问", description = "图片资源访问接口")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageStorageService imageStorageService;

    @Operation(summary = "获取图片", description = "根据posterUrl获取图片流")
    @GetMapping
    public void getImage(
            @Parameter(description = "图片相对路径（posterUrl）") @RequestParam String path,
            HttpServletResponse response) throws Exception {

        log.info("获取图片，path: {}", path);

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
