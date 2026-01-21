package cn.tannn.lychnos.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
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
     * 配置的密钥（由 Spring 注入）
     * 通过 application.yaml 中的 app.security.aes-secret-key 配置
     */
    private static String configuredSecretKey;

    /**
     * 设置密钥（由 Spring 配置类调用）
     *
     * @param secretKey 配置的密钥
     */
    public static void setSecretKey(String secretKey) {
        configuredSecretKey = secretKey;
    }

    /**
     * 从任意字符串生成 16 字节的 AES 密钥
     * 使用 MD5 哈希算法将任意长度的字符串转换为 16 字节
     *
     * @param seed 种子字符串
     * @return 16 字节的密钥字符串
     */
    public static String generateSecretKey(String seed) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(seed.getBytes(StandardCharsets.UTF_8));
            // MD5 生成 16 字节的哈希值，正好符合 AES-128 的要求
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("生成密钥失败", e);
            throw new RuntimeException("生成密钥失败: " + e.getMessage(), e);
        }
    }

    /**
     * 打印配置示例（用于生成配置文件内容）
     * 使用方式：在启动类中调用此方法，然后将输出复制到 application.yaml
     *
     * @param seed 种子字符串（建议使用项目名称或其他唯一标识）
     */
    public static void printConfigExample(String seed) {
        String secretKey = generateSecretKey(seed);
        System.out.println("\n=================================================");
        System.out.println("AES 加密密钥配置示例（请复制到 application.yaml）：");
        System.out.println("=================================================");
        System.out.println("app:");
        System.out.println("  security:");
        System.out.println("    aes-secret-key: " + secretKey);
        System.out.println("=================================================\n");
    }

    /**
     * 获取密钥的 16 字节形式
     *
     * @param secretKey 密钥字符串
     * @return 16 字节的密钥数组
     */
    private static byte[] getKeyBytes(String secretKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            // 确保是 16 字节（AES-128）
            return Arrays.copyOf(keyBytes, 16);
        } catch (IllegalArgumentException e) {
            log.warn("配置的密钥不是有效的 Base64 格式，将使用原始字符串的 UTF-8 编码。错误信息: {}", e.getMessage());
            // 如果不是 Base64，则使用原始字符串的 UTF-8 编码
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            return Arrays.copyOf(keyBytes, 16);
        } catch (Exception e) {
            log.error("处理密钥时发生未知错误，尝试使用 UTF-8 编码", e);
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            return Arrays.copyOf(keyBytes, 16);
        }
    }

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @return 加密后的 Base64 编码字符串
     */
    public static String encrypt(String plainText) {
        if (configuredSecretKey == null || configuredSecretKey.isEmpty()) {
            throw new IllegalStateException("未配置 AES 密钥，请在 application.yaml 中配置 app.security.aes-secret-key");
        }
        return encrypt(plainText, configuredSecretKey);
    }

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @param secretKey 密钥
     * @return 加密后的 Base64 编码字符串
     */
    public static String encrypt(String plainText, String secretKey) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            // 创建密钥
            byte[] keyBytes = getKeyBytes(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

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
        if (configuredSecretKey == null || configuredSecretKey.isEmpty()) {
            throw new IllegalStateException("未配置 AES 密钥，请在 application.yaml 中配置 app.security.aes-secret-key");
        }
        return decrypt(encryptedText, configuredSecretKey);
    }

    /**
     * 解密字符串
     *
     * @param encryptedText 加密后的 Base64 编码字符串
     * @param secretKey     密钥
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText, String secretKey) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            // 创建密钥
            byte[] keyBytes = getKeyBytes(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

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
