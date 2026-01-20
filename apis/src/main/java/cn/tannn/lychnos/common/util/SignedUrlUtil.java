package cn.tannn.lychnos.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 签名 URL 工具类
 * <p>
 * 用于生成和验证带时效性的签名 URL，允许未登录用户安全访问图片资源
 * <p>
 * 安全机制：
 * - 使用 HMAC-SHA256 算法生成签名
 * - 签名包含路径和过期时间，防止篡改
 * - 过期时间验证，确保 URL 时效性
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/20
 */
@Slf4j
public class SignedUrlUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * 默认签名有效期：30 分钟（毫秒）
     */
    private static final long DEFAULT_EXPIRY_MS = 30 * 60 * 1000L;

    /**
     * 生成签名 URL
     *
     * @param path      图片路径（posterUrl）
     * @param secretKey 签名密钥
     * @return 签名参数字符串（格式：expires=xxx&signature=yyy）
     */
    public static String generateSignature(String path, String secretKey) {
        return generateSignature(path, secretKey, DEFAULT_EXPIRY_MS);
    }

    /**
     * 生成签名 URL
     *
     * @param path      图片路径（posterUrl）
     * @param secretKey 签名密钥
     * @param expiryMs  有效期（毫秒）
     * @return 签名参数字符串（格式：expires=xxx&signature=yyy）
     */
    public static String generateSignature(String path, String secretKey, long expiryMs) {
        try {
            // 计算过期时间戳（秒）
            long expiresAt = (System.currentTimeMillis() + expiryMs) / 1000;

            // 生成签名内容：path + expires
            String signatureContent = path + ":" + expiresAt;

            // 使用 HMAC-SHA256 生成签名
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(signatureContent.getBytes(StandardCharsets.UTF_8));

            // Base64 编码签名
            String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

            // 返回签名参数
            return "expires=" + expiresAt + "&signature=" + URLEncoder.encode(signature, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 验证签名 URL
     *
     * @param path      图片路径（posterUrl）
     * @param expires   过期时间戳（秒）
     * @param signature 签名
     * @param secretKey 签名密钥
     * @return 是否有效
     */
    public static boolean verifySignature(String path, long expires, String signature, String secretKey) {
        try {
            // 1. 检查是否过期
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime > expires) {
                log.warn("签名已过期，当前时间: {}, 过期时间: {}", currentTime, expires);
                return false;
            }

            // 2. 重新生成签名并比对
            String signatureContent = path + ":" + expires;
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(signatureContent.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

            // 3. 比对签名（防止时序攻击）
            boolean isValid = constantTimeEquals(expectedSignature, signature);

            if (!isValid) {
                log.warn("签名验证失败，路径: {}", path);
            }

            return isValid;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("验证签名失败", e);
            return false;
        }
    }

    /**
     * 常量时间字符串比较（防止时序攻击）
     *
     * @param a 字符串 a
     * @param b 字符串 b
     * @return 是否相等
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }

        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        if (aBytes.length != bBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }

        return result == 0;
    }
}
