package cn.tannn.lychnos.common.config;

import cn.tannn.lychnos.common.util.AESUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 安全配置
 * 用于初始化 AES 加密密钥
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/17
 */
@Configuration
@Slf4j
public class SecurityConfig {

    /**
     * AES 加密密钥
     * 从 application.yaml 中读取 app.security.aes-secret-key 配置
     */
    @Value("${app.security.aes-secret-key:}")
    private String aesSecretKey;

    /**
     * 初始化 AES 密钥
     */
    @PostConstruct
    public void initAESKey() {
        if (aesSecretKey == null || aesSecretKey.isEmpty()) {
            log.error("=================================================");
            log.error("未配置 AES 加密密钥！");
            log.error("请在 application.yaml 中添加以下配置：");
            log.error("=================================================");
            log.error("app:");
            log.error("  security:");
            log.error("    aes-secret-key: <你的密钥>");
            log.error("=================================================");
            log.error("可以使用以下方法生成密钥：");
            log.error("AESUtil.printConfigExample(\"Lychnos2026\")");
            log.error("=================================================");
            throw new IllegalStateException("未配置 AES 加密密钥，请在 application.yaml 中配置 app.security.aes-secret-key");
        }

        // 设置到 AESUtil
        AESUtil.setSecretKey(aesSecretKey);
        log.info("AES 加密密钥初始化成功");
    }
}
