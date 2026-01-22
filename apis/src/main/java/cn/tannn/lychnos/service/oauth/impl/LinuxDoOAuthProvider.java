package cn.tannn.lychnos.service.oauth.impl;

import cn.tannn.lychnos.common.util.AESUtil;
import cn.tannn.lychnos.entity.OAuthConfig;
import cn.tannn.lychnos.service.oauth.OAuth2Provider;
import cn.tannn.lychnos.service.oauth.OAuth2UserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * LinuxDo OAuth2 Provider 实现
 *
 * 注意：LinuxDo 的具体 OAuth2 端点待确认
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LinuxDoOAuthProvider implements OAuth2Provider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private OAuthConfig config;

    /**
     * 设置配置（由 OAuth2Service 注入）
     *
     * @param config OAuth2配置
     */
    public void setConfig(OAuthConfig config) {
        this.config = config;
    }

    @Override
    public String getProviderType() {
        return "LINUXDO";
    }

    @Override
    public String getAuthorizeUrl(String state, String redirectUri) {
        if (config == null) {
            throw new IllegalStateException("LinuxDo OAuth2 配置未初始化");
        }

        return UriComponentsBuilder.fromUriString(config.getAuthorizeUrl())
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", StringUtils.isNotBlank(config.getScope()) ? config.getScope() : "read")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public String getAccessToken(String code, String redirectUri) {
        if (config == null) {
            throw new IllegalStateException("LinuxDo OAuth2 配置未初始化");
        }

        try {
            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", config.getClientId());
            params.add("client_secret", config.getClientSecret());
            params.add("code", code);
            params.add("grant_type", "authorization_code");
            params.add("redirect_uri", redirectUri);

            // Debug Logging
            log.info("LinuxDo Token Request - ClientID: {}, SecretLength: {}, RedirectURI: {}",
                    AESUtil.maskText(config.getClientId()),
                    (config.getClientSecret() != null ? config.getClientSecret().length() : 0),
                    redirectUri);

            // 设置请求头 - 不使用 Basic Auth
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getTokenUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("LinuxDo Token Response: {}", response.getBody());
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String accessToken = jsonNode.get("access_token").asText();
                log.info("LinuxDo 获取 Access Token 成功");
                return accessToken;
            } else {
                log.error("LinuxDo 获取 Access Token 失败 - Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("获取 LinuxDo Access Token 失败");
            }
        } catch (HttpClientErrorException e) {
            log.error("LinuxDo HTTP 错误 - Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("获取 LinuxDo Access Token 异常: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("LinuxDo 获取 Access Token 异常", e);
            throw new RuntimeException("获取 LinuxDo Access Token 异常", e);
        }
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        if (config == null) {
            throw new IllegalStateException("LinuxDo OAuth2 配置未初始化");
        }

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getUserInfoUrl(),
                    HttpMethod.GET,
                    requestEntity,
                    String.class);

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                OAuth2UserInfo userInfo = new OAuth2UserInfo();

                // LinuxDo 返回的字段（根据用户提供的文档）
                // 可获取字段：id, username, name, avatar_template, active, trust_level, silenced,
                // external_ids, api_key
                userInfo.setOpenId(jsonNode.get("id").asText());
                userInfo.setNickname(jsonNode.has("username") ? jsonNode.get("username").asText() : null);

                // LinuxDo 的头像模板
                if (jsonNode.has("avatar_template")) {
                    String avatarTemplate = jsonNode.get("avatar_template").asText();
                    // avatar_template 格式: //linux.do/user_avatar/linux.do/{username}/{size}/1_2.png
                    // 替换 {size} 为 120（中等尺寸）
                    String avatarUrl = avatarTemplate.replace("{size}", "120");
                    if (!avatarUrl.startsWith("http")) {
                        avatarUrl = "https:" + avatarUrl;
                    }
                    userInfo.setAvatarUrl(avatarUrl);
                }

                // 保存额外信息
                if (jsonNode.has("name") && !jsonNode.get("name").isNull()) {
                    userInfo.addExtraInfo("name", jsonNode.get("name").asText());
                }
                if (jsonNode.has("active")) {
                    userInfo.addExtraInfo("active", jsonNode.get("active").asBoolean());
                }
                if (jsonNode.has("trust_level")) {
                    userInfo.addExtraInfo("trust_level", jsonNode.get("trust_level").asInt());
                }
                if (jsonNode.has("silenced")) {
                    userInfo.addExtraInfo("silenced", jsonNode.get("silenced").asBoolean());
                }

                log.info("LinuxDo 获取用户信息成功: {}", userInfo.getNickname());
                return userInfo;
            } else {
                log.error("LinuxDo 获取用户信息失败: {}", response.getBody());
                throw new RuntimeException("获取 LinuxDo 用户信息失败");
            }
        } catch (Exception e) {
            log.error("LinuxDo 获取用户信息异常", e);
            throw new RuntimeException("获取 LinuxDo 用户信息异常", e);
        }
    }
}
