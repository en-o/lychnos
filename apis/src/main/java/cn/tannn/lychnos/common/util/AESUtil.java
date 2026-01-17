package cn.tannn.lychnos.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加密/解密工具类
 * 用于敏感信息（如 API Key）的加密存储
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/17
 */
@Slf4j
public class AESUtil {

    /**
     * AES 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * AES 加密模式
     */
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 默认密钥（16字节）
     * 生产环境建议通过配置文件或环境变量配置，并定期更换
     */
    private static final String DEFAULT_SECRET_KEY = "Lychnos2026Key!!";

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @return 加密后的 Base64 编码字符串
     */
    public static String encrypt(String plainText) {
        return encrypt(plainText, DEFAULT_SECRET_KEY);
    }

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @param secretKey 密钥（必须是16、24或32字节）
     * @return 加密后的 Base64 编码字符串
     */
    public static String encrypt(String plainText, String secretKey) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            // 创建密钥
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            // 创建密码器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // 加密
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Base64 编码
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES 加密失败", e);
            throw new RuntimeException("加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密字符串
     *
     * @param encryptedText 加密后的 Base64 编码字符串
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText) {
        return decrypt(encryptedText, DEFAULT_SECRET_KEY);
    }

    /**
     * 解密字符串
     *
     * @param encryptedText 加密后的 Base64 编码字符串
     * @param secretKey 密钥（必须是16、24或32字节）
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText, String secretKey) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            // 创建密钥
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            // 创建密码器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // Base64 解码
            byte[] encrypted = Base64.getDecoder().decode(encryptedText);

            // 解密
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 解密失败", e);
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将字符串掩码显示（用于前端显示）
     *
     * @param text 原始文本
     * @return 掩码后的文本（如：sk-abc***xyz）
     */
    public static String maskText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        int length = text.length();

        // 短文本直接显示为 ****
        if (length <= 8) {
            return "****";
        }

        // 显示前4个和后4个字符，中间用 *** 代替
        String prefix = text.substring(0, 4);
        String suffix = text.substring(length - 4);
        return prefix + "***" + suffix;
    }
}
