package cn.tannn.lychnos.service.oauth;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

/**
 * OAuth2 用户信息 DTO
 * 统一封装不同第三方平台返回的用户信息
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Data
public class OAuth2UserInfo {

    /**
     * 第三方平台用户唯一标识（必须）
     */
    private String openId;

    /**
     * 第三方平台UnionID（可选，如微信）
     */
    private String unionId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 其他额外信息（JSON对象）
     */
    private JSONObject extraInfo = new JSONObject();

    /**
     * 添加额外信息
     *
     * @param key   键
     * @param value 值
     */
    public void addExtraInfo(String key, Object value) {
        if (this.extraInfo == null) {
            this.extraInfo = new JSONObject();
        }
        this.extraInfo.put(key, value);
    }
}
