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
     * @return 相对路径（不含前缀），格式：年月日/书籍名.png
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

            // 返回相对路径：年月日/书籍名.png
            String relativePath = dateDir + "/" + fileName;
            log.info("图片保存成功，相对路径: {}", relativePath);

            return relativePath;
        } catch (Exception e) {
            log.error("保存图片失败，书籍: {}", bookTitle, e);
            throw new RuntimeException("保存图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据相对路径读取图片
     *
     * @param relativePath 相对路径（年月日/书籍名.png）
     * @return 图片输入流
     */
    public InputStream getImage(String relativePath) {
        try {
            Path filePath = Paths.get(storagePath, relativePath);
            File file = filePath.toFile();

            if (!file.exists()) {
                log.warn("图片文件不存在: {}", relativePath);
                return null;
            }

            return Files.newInputStream(filePath);
        } catch (Exception e) {
            log.error("读取图片失败，路径: {}", relativePath, e);
            throw new RuntimeException("读取图片失败: " + e.getMessage(), e);
        }
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
