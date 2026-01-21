package cn.tannn.lychnos.common.util;

import org.junit.jupiter.api.Test;

class AESUtilTest {

    @Test
    void generateSecretKey() {
        System.out.println(AESUtil.generateSecretKey("Lychnos2026SecretKey"));
    }
}
