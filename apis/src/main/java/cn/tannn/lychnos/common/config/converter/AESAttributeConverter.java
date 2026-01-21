package cn.tannn.lychnos.common.config.converter;

import cn.tannn.lychnos.common.util.AESUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

/**
 * JPA 属性转换器 - AES 加密/解密
 * 用于实体字段的自动加密存储和解密读取
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Converter
public class AESAttributeConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (!StringUtils.hasText(attribute)) {
            return attribute;
        }
        try {
            return AESUtil.encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("加密数据失败", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return dbData;
        }
        try {
            return AESUtil.decrypt(dbData);
        } catch (Exception e) {
            // 如果解密失败（可能是明文或密钥错误），返回原值或抛出异常
            // 这里为了容错，如果解密失败则返回原值（假设是明文）
            // 但在生产环境应严格处理
            return dbData;
        }
    }
}
