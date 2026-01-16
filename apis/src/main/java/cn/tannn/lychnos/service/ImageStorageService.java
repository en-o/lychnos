package cn.tannn.lychnos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 图片存储服务
 * <p>
 * 支持统一格式：协议:鉴权:路径
 * - 协议: h(HTTP/HTTPS) / ali(阿里云OSS) / qiniu(七牛云) / s3(AWS S3) / f(FTP) / l(本地)
 * - 鉴权: 0(无需鉴权) / 1(需要鉴权)
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/17
 */
@Service
@Slf4j
public class ImageStorageService {

    @Value("${app.image.storage-path}")
    private String storagePath;

    /**
     * 存储图片到本地
     * 存储路径格式：{storagePath}/年月日/书籍名.png
     *
     * @param inputStream 图片输入流
     * @param bookTitle   书籍名称
     * @return poster_url 格式（l:1:/年月日/书籍名.png）
     */
    public String saveImage(InputStream inputStream, String bookTitle) {
        try {
            // 生成日期目录：yyyyMMdd
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 清理书籍名称，移除特殊字符
            String sanitizedTitle = sanitizeFileName(bookTitle);

            // 生成文件名：书籍名.png
            String fileName = sanitizedTitle + ".png";

            // 构建完整存储路径
            Path dirPath = Paths.get(storagePath, dateDir);
            Path filePath = dirPath.resolve(fileName);

            // 创建目录（如果不存在）
            Files.createDirectories(dirPath);

            // 保存文件
            try (OutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // 返回统一格式：l:1:/年月日/书籍名.png
            String posterUrl = "l:1:/" + dateDir + "/" + fileName;
            log.info("图片保存成功，posterUrl: {}", posterUrl);

            return posterUrl;
        } catch (Exception e) {
            log.error("保存图片失败，书籍: {}", bookTitle, e);
            throw new RuntimeException("保存图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据 posterUrl 读取图片
     * <p>
     * 格式：协议:鉴权:路径
     * 示例：
     * - h:0:https://example.com/image.png (无鉴权HTTP，不应通过此方法访问)
     * - l:1:/20240115/三体.png (本地存储)
     * - ali:1:/bucket/path/image.png (阿里云OSS，待实现)
     *
     * @param posterUrl poster_url 格式字符串
     * @return 图片输入流
     */
    public InputStream getImage(String posterUrl) {
        try {
            // 解析格式：协议:鉴权:路径
            String[] parts = posterUrl.split(":", 3);

            // 兼容旧格式（直接是路径）
            if (parts.length < 3) {
                return getLocalImageByPath(posterUrl);
            }

            String protocol = parts[0];
            String auth = parts[1];
            String path = parts[2];

            // 根据协议类型处理
            return switch (protocol) {
                case "l" -> getLocalImageByPath(path);
                case "h" -> getHttpImage(auth, path);
                case "ali" -> getAliOssImage(auth, path);
                case "qiniu" -> getQiniuImage(auth, path);
                case "s3" -> getS3Image(auth, path);
                case "f" -> getFtpImage(auth, path);
                default -> throw new IllegalArgumentException("不支持的协议: " + protocol);
            };

        } catch (Exception e) {
            log.error("读取图片失败，posterUrl: {}", posterUrl, e);
            throw new RuntimeException("读取图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 读取本地图片
     *
     * @param path 本地路径（/年月日/书籍名.png）
     * @return 图片输入流
     */
    private InputStream getLocalImageByPath(String path) {
        try {
            // 移除开头的斜杠
            String relativePath = path.startsWith("/") ? path.substring(1) : path;

            Path filePath = Paths.get(storagePath, relativePath);
            File file = filePath.toFile();

            if (!file.exists()) {
                log.warn("图片文件不存在: {}", relativePath);
                return null;
            }

            return Files.newInputStream(filePath);
        } catch (Exception e) {
            log.error("读取本地图片失败，路径: {}", path, e);
            throw new RuntimeException("读取本地图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 读取 HTTP/HTTPS 图片（待实现）
     */
    private InputStream getHttpImage(String auth, String path) {
        throw new UnsupportedOperationException("HTTP/HTTPS 图片读取功能待实现");
    }

    /**
     * 读取阿里云 OSS 图片（待实现）
     */
    private InputStream getAliOssImage(String auth, String path) {
        throw new UnsupportedOperationException("阿里云 OSS 图片读取功能待实现");
    }

    /**
     * 读取七牛云图片（待实现）
     */
    private InputStream getQiniuImage(String auth, String path) {
        throw new UnsupportedOperationException("七牛云图片读取功能待实现");
    }

    /**
     * 读取 AWS S3 图片（待实现）
     */
    private InputStream getS3Image(String auth, String path) {
        throw new UnsupportedOperationException("AWS S3 图片读取功能待实现");
    }

    /**
     * 读取 FTP 图片（待实现）
     */
    private InputStream getFtpImage(String auth, String path) {
        throw new UnsupportedOperationException("FTP 图片读取功能待实现");
    }

    /**
     * 清理文件名，移除特殊字符
     *
     * @param fileName 原始文件名
     * @return 清理后的文件名
     */
    private String sanitizeFileName(String fileName) {
        // 移除或替换特殊字符
        return fileName
                .replaceAll("[\\\\/:*?\"<>|]", "_")  // 替换 Windows 不允许的字符
                .replaceAll("\\s+", "_")              // 空格替换为下划线
                .trim();
    }
}
