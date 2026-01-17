package cn.tannn.lychnos.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AESUtilTest {

    @Test
    void generateSecretKey() {
        System.out.println(AESUtil.generateSecretKey("Lychnos2026SecretKey"));
    }
}
