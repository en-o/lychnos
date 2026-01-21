package cn.tannn.lychnos.service.oauth;

/**
 * OAuth2 Provider 接口
 * 使用策略模式，为不同的第三方平台提供统一的OAuth2接口
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
public interface OAuth2Provider {

    /**
     * 获取平台类型标识
     *
     * @return 平台类型（如 GITHUB、LINUXDO）
     */
    String getProviderType();

    /**
     * 生成OAuth2授权URL
     *
     * @param state       状态码（用于防止CSRF攻击）
     * @param redirectUri 回调地址
     * @return 授权URL
     */
    String getAuthorizeUrl(String state, String redirectUri);

    /**
     * 使用授权码换取访问令牌
     *
     * @param code        授权码
     * @param redirectUri 回调地址（必须与授权时的一致）
     * @return 访问令牌
     */
    String getAccessToken(String code, String redirectUri);

    /**
     * 使用访问令牌获取用户信息
     *
     * @param accessToken 访问令牌
     * @return OAuth2用户信息
     */
    OAuth2UserInfo getUserInfo(String accessToken);
}
