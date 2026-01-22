package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.exception.built.BusinessException;
import cn.tannn.jdevelops.exception.built.UserException;
import cn.tannn.jdevelops.jwt.standalone.service.LoginService;
import cn.tannn.lychnos.common.util.AESUtil;
import cn.tannn.lychnos.common.util.UserUtil;
import cn.tannn.lychnos.controller.vo.LoginVO;
import cn.tannn.lychnos.dao.UserThirdPartyBindDao;
import cn.tannn.lychnos.entity.OAuthConfig;
import cn.tannn.lychnos.entity.UserInfo;
import cn.tannn.lychnos.entity.UserThirdPartyBind;
import cn.tannn.lychnos.enums.ProviderType;
import cn.tannn.lychnos.service.oauth.OAuth2Provider;
import cn.tannn.lychnos.service.oauth.OAuth2UserInfo;
import cn.tannn.lychnos.service.oauth.impl.GitHubOAuthProvider;
import cn.tannn.lychnos.service.oauth.impl.LinuxDoOAuthProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

/**
 * OAuth2 核心服务类
 * 协调各个 OAuth2 Provider，处理登录、绑定、解绑等核心业务逻辑
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2Service {

    private final OAuthConfigService oauthConfigService;
    private final UserInfoService userInfoService;
    private final UserThirdPartyBindDao bindDao;
    private final LoginService loginService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Provider 实例
    private final GitHubOAuthProvider gitHubProvider;
    private final LinuxDoOAuthProvider linuxDoProvider;

    // Provider 映射
    private final Map<String, OAuth2Provider> providerMap = new HashMap<>();

    /**
     * 第三方登录授权回调当前项目的地址
     */
    @Value("${app.oauth.callback-base-url:http://localhost:1250}")
    private String callbackBaseUrl;

    /**
     * 初始化 Provider 映射
     */
    @PostConstruct
    public void initProviders() {
        providerMap.put("GITHUB", gitHubProvider);
        providerMap.put("LINUXDO", linuxDoProvider);
        log.info("OAuth2 Providers 初始化完成：{}", providerMap.keySet());
    }

    /**
     * 获取 Provider
     *
     * @param providerType 平台类型
     * @return OAuth2Provider
     */
    private OAuth2Provider getProvider(ProviderType providerType) {
        OAuth2Provider provider = providerMap.get(providerType.getValue());
        if (provider == null) {
            throw new BusinessException("不支持的第三方平台：" + providerType);
        }

        // 从数据库加载配置并注入到 Provider
        Optional<OAuthConfig> configOpt = oauthConfigService.getConfigByType(providerType);
        if (configOpt.isEmpty()) {
            throw new BusinessException("第三方平台未配置：" + providerType);
        }

        // 使用反射或直接调用 setConfig 方法注入配置
        if (provider instanceof GitHubOAuthProvider) {
            ((GitHubOAuthProvider) provider).setConfig(configOpt.get());
        } else if (provider instanceof LinuxDoOAuthProvider) {
            ((LinuxDoOAuthProvider) provider).setConfig(configOpt.get());
        }

        return provider;
    }

    /**
     * 生成授权URL
     *
     * @param providerType 平台类型
     * @param state        状态码（用于防CSRF攻击）
     * @return 授权URL
     */
    /**
     * 生成授权URL
     *
     * @param providerType 平台类型
     * @param loginName    当前登录用户名（可选，用于绑定）
     * @return 授权URL
     */
    public String generateAuthorizeUrl(ProviderType providerType, String loginName) {
        OAuth2Provider provider = getProvider(providerType);

        // 构造状态码 (State)
        // 格式: Action|Data|UUID
        // - 登录: LOGIN||UUID
        // - 绑定: BIND|loginName|UUID
        String rawState;
        if (StringUtils.isNotBlank(loginName)) {
            rawState = "BIND|" + loginName + "|" + UUID.randomUUID().toString();
        } else {
            rawState = "LOGIN||" + UUID.randomUUID().toString();
        }

        // 加密 State 防止篡改
        String state = AESUtil.encrypt(rawState);

        // 构造回调地址
        String redirectUri = StringUtils.stripEnd(callbackBaseUrl, "/") + "/oauth/callback/"
                + providerType.getValue().toLowerCase();

        return provider.getAuthorizeUrl(state, redirectUri);
    }

    /**
     * 处理OAuth2回调，完成登录或绑定
     * <p>
     * 此方法统一处理第三方平台的授权回调，支持两种场景：
     * 1. 第三方登录（LOGIN）：
     *    - 如果第三方账号已绑定系统用户，直接登录
     *    - 如果第三方账号未绑定，创建新用户并自动绑定
     * 2. 绑定第三方账户（BIND）：
     *    - 将第三方账号绑定到指定的系统用户
     *    - 绑定成功后自动登录该用户
     * </p>
     * <p>
     * 注意：绑定操作统一由此方法处理，不再使用独立的 bindThirdPartyAccount 方法。
     * 前端应通过 OAuth2LoginController#getAuthorizeUrl 生成授权URL（携带loginName参数表示绑定），
     * 然后第三方平台回调到 OAuth2LoginController#handleCallback，由此方法统一处理。
     * </p>
     *
     * @param providerType 平台类型
     * @param code         授权码
     * @param state        状态码（用于验CSRF和传递上下文，格式：ACTION|loginName|UUID）
     * @return 登录Token
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO handleCallback(ProviderType providerType, String code, String state) {
        // 1. 解析 State
        String action = "LOGIN";
        String targetLoginName = null;

        try {
            if (StringUtils.isNotBlank(state)) {
                String decryptedState = AESUtil.decrypt(state);
                String[] parts = decryptedState.split("\\|");
                if (parts.length >= 1) {
                    action = parts[0];
                }
                if (parts.length >= 2) {
                    targetLoginName = parts[1];
                }
            }
        } catch (Exception e) {
            log.warn("OAuth State 解密失败或格式错误，降级为普通登录流程。State: {}", state);
        }

        OAuth2Provider provider = getProvider(providerType);

        // 2. 获取 Access Token
        String redirectUri = StringUtils.stripEnd(callbackBaseUrl, "/") + "/oauth/callback/"
                + providerType.getValue().toLowerCase();
        String accessToken = provider.getAccessToken(code, redirectUri);

        // 3. 获取第三方用户信息
        OAuth2UserInfo oauthUserInfo = provider.getUserInfo(accessToken);

        // 4. 根据 Action 执行不同逻辑
        if ("BIND".equals(action) && StringUtils.isNotBlank(targetLoginName)) {
            return handleBindCallback(targetLoginName, oauthUserInfo, providerType);
        } else {
            return handleLoginCallback(oauthUserInfo, providerType);
        }
    }

    /**
     * 处理绑定回调
     */
    private LoginVO handleBindCallback(String loginName, OAuth2UserInfo oauthUserInfo, ProviderType providerType) {
        // 1. 查找目标用户
        UserInfo userInfo = userInfoService.findByLoginName(loginName)
                .orElseThrow(() -> new UserException("绑定目标用户不存在: " + loginName));

        // 2. 检查该第三方账号是否已被其他用户绑定
        Optional<UserThirdPartyBind> existingBind = bindDao.findByProviderTypeAndOpenId(
                providerType,
                oauthUserInfo.getOpenId());

        if (existingBind.isPresent()) {
            if (!existingBind.get().getUserId().equals(userInfo.getId())) {
                throw new BusinessException("该第三方账号已被其他用户绑定，无法绑定当前账号");
            } else {
                // 已经是当前用户绑定的，更新信息即可
                updateBindInfo(existingBind.get(), oauthUserInfo);
            }
        } else {
            // 3. 检查该用户是否已绑定过此同类平台 (防止重复绑定同个平台账号，虽然UI限制了，但后端要兜底)
            // 允许一个用户绑定多个同平台账号？通常不允许。检查 existsByUserIdAndProviderType
            // 但这里我们是在 OAuth 回调中，如果是 switch account 场景...
            // 简单起见，如果该用户已经绑定了 GITHUB，则解绑旧的？或者报错？
            // 现在的逻辑是 ProfilePage 显示 "已绑定/未绑定"。
            // 如果已绑定，前端显示"解绑"。 handleBind 应该是用于"未绑定"状态。
            // 检查是否已绑定:
            Optional<UserThirdPartyBind> userBind = bindDao.findByUserIdAndProviderType(userInfo.getId(), providerType);
            if (userBind.isPresent()) {
                // 可能是重复操作，或者换绑？这里我们认为是更新
                // 但前面检查了 openId。如果 openId 不同，说明用户在第三方平台换了个号登录。
                // 这种情况下，应该报错"您已绑定过一个GitHub账号，请先解绑"。
                if (!userBind.get().getOpenId().equals(oauthUserInfo.getOpenId())) {
                    throw new BusinessException("您已绑定过该平台账号，请先解绑原账号");
                }
            } else {
                // 创建新绑定
                createBind(userInfo.getId(), providerType, oauthUserInfo);
            }
        }

        // 绑定成功，自动登录该用户
        String token = UserUtil.generateLoginToken(loginService, userInfo);
        return new LoginVO(token);
    }

    /**
     * 处理登录回调
     */
    private LoginVO handleLoginCallback(OAuth2UserInfo oauthUserInfo, ProviderType providerType) {
        // 检查该第三方账号是否已绑定系统账户
        Optional<UserThirdPartyBind> bindOpt = bindDao.findByProviderTypeAndOpenId(
                providerType,
                oauthUserInfo.getOpenId());

        UserInfo userInfo;
        if (bindOpt.isPresent()) {
            // 已绑定，直接登录
            UserThirdPartyBind bind = bindOpt.get();
            userInfo = userInfoService.getJpaBasicsDao().findById(bind.getUserId())
                    .orElseThrow(() -> new UserException("用户不存在"));

            log.info("第三方账号已绑定，用户ID：{}，第三方平台：{}", userInfo.getId(), providerType);

            // 更新绑定信息
            updateBindInfo(bind, oauthUserInfo);
        } else {
            // 未绑定，创建新系统账户并绑定
            userInfo = createUserForOAuth(oauthUserInfo, providerType);
            log.info("第三方账号首次登录，创建新用户：{}，第三方平台：{}", userInfo.getId(), providerType);
        }

        // 生成 JWT Token 返回
        String token = UserUtil.generateLoginToken(loginService, userInfo);
        return new LoginVO(token);
    }

    private void createBind(Long userId, ProviderType providerType, OAuth2UserInfo oauthUserInfo) {
        UserThirdPartyBind bind = new UserThirdPartyBind();
        bind.setUserId(userId);
        bind.setProviderType(providerType);
        bind.setOpenId(oauthUserInfo.getOpenId());
        bind.setUnionId(oauthUserInfo.getUnionId());
        bind.setNickname(oauthUserInfo.getNickname());
        bind.setAvatarUrl(oauthUserInfo.getAvatarUrl());
        bind.setEmail(oauthUserInfo.getEmail());

        try {
            if (oauthUserInfo.getExtraInfo() != null && !oauthUserInfo.getExtraInfo().isEmpty()) {
                bind.setExtraInfo(oauthUserInfo.getExtraInfo());
            }
        } catch (Exception e) {
            log.warn("转换 extraInfo 为 JSON 失败", e);
        }

        bindDao.save(bind);
        log.info("绑定第三方账号成功：用户ID={}, 平台={}, OpenID={}", userId, providerType, oauthUserInfo.getOpenId());
    }

    /**
     * 解绑第三方账户
     *
     * @param userId       用户ID
     * @param providerType 平台类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbindThirdPartyAccount(Long userId, ProviderType providerType) {
        // 1. 检查绑定关系是否存在
        UserThirdPartyBind bind = bindDao.findByUserIdAndProviderType(userId, providerType)
                .orElseThrow(() -> new BusinessException("未找到绑定关系"));

        // 2. 检查解绑后用户是否还有登录方式
        UserInfo userInfo = userInfoService.getJpaBasicsDao().findById(userId)
                .orElseThrow(() -> new UserException("用户不存在"));

        boolean hasPassword = StringUtils.isNotBlank(userInfo.getPassword());
        long bindCount = bindDao.findByUserId(userId).size();

        if (!hasPassword && bindCount <= 1) {
            throw new BusinessException("无法解绑，您必须先设置密码或绑定其他第三方账号");
        }

        // 3. 删除绑定记录
        bindDao.delete(bind);
        log.info("解绑第三方账号成功：用户ID={}, 平台={}", userId, providerType);
    }

    /**
     * 获取用户已绑定的第三方账户列表
     *
     * @param userId 用户ID
     * @return 绑定列表
     */
    public List<UserThirdPartyBind> getUserBindings(Long userId) {
        return bindDao.findByUserId(userId);
    }

    // ============ 私有辅助方法 ============

    /**
     * 为第三方登录创建新用户或绑定到已有用户
     * <p>
     * 处理场景：
     * 1. 用户首次通过第三方登录 → 创建新用户并绑定
     * 2. 用户曾通过第三方登录创建账户，后来解绑，再次登录 → 直接绑定到原账户
     * </p>
     *
     * @param oauthUserInfo 第三方用户信息
     * @param providerType  平台类型
     * @return 用户信息（新创建或已存在）
     */
    private UserInfo createUserForOAuth(OAuth2UserInfo oauthUserInfo, ProviderType providerType) {
        // 生成登录名（使用平台类型 + OpenID）
        String loginName = providerType.getValue().toLowerCase() + "_" + oauthUserInfo.getOpenId();

        // 检查该登录名是否已存在（可能是用户解绑后重新登录）
        Optional<UserInfo> existingUser = userInfoService.findByLoginName(loginName);

        UserInfo userInfo;
        if (existingUser.isPresent()) {
            // 用户已存在，直接使用（可能是解绑后重新登录）
            userInfo = existingUser.get();
            log.info("检测到已存在的用户，直接绑定：用户名={}, 用户ID={}", loginName, userInfo.getId());
        } else {
            // 用户不存在，创建新用户
            userInfo = new UserInfo();
            userInfo.setLoginName(loginName);

            // 昵称（优先使用 nickname，如果为空则使用 loginName）
            String nickname = StringUtils.isNotBlank(oauthUserInfo.getNickname())
                    ? oauthUserInfo.getNickname()
                    : "用户" + oauthUserInfo.getOpenId().substring(0, Math.min(6, oauthUserInfo.getOpenId().length()));
            userInfo.setNickname(nickname);

            // 邮箱
            userInfo.setEmail(oauthUserInfo.getEmail());

            // 设置默认角色
            userInfo.setRoles(UserInfoService.createDefaultRoles());

            // 为第三方登录用户生成随机强密码（用户可后续修改）
            String randomPassword = generateSecureRandomPassword();
            userInfo.setPassword(UserInfo.getMd5Password(loginName, randomPassword));

            log.info("为第三方登录用户生成随机密码，用户名：{}", loginName);

            // 保存用户
            userInfo = userInfoService.getJpaBasicsDao().save(userInfo);
        }

        // 创建绑定关系
        UserThirdPartyBind bind = new UserThirdPartyBind();
        bind.setUserId(userInfo.getId());
        bind.setProviderType(providerType);
        bind.setOpenId(oauthUserInfo.getOpenId());
        bind.setUnionId(oauthUserInfo.getUnionId());
        bind.setNickname(oauthUserInfo.getNickname());
        bind.setAvatarUrl(oauthUserInfo.getAvatarUrl());
        bind.setEmail(oauthUserInfo.getEmail());

        // 转换额外信息为 JSON
        try {
            if (oauthUserInfo.getExtraInfo() != null && !oauthUserInfo.getExtraInfo().isEmpty()) {
                bind.setExtraInfo(oauthUserInfo.getExtraInfo());
            }
        } catch (Exception e) {
            log.warn("转换 extraInfo 为 JSON 失败", e);
        }

        bindDao.save(bind);

        return userInfo;
    }

    /**
     * 更新绑定信息（昵称、头像等可能变化）
     *
     * @param bind          绑定记录
     * @param oauthUserInfo 最新的第三方用户信息
     */
    private void updateBindInfo(UserThirdPartyBind bind, OAuth2UserInfo oauthUserInfo) {
        boolean updated = false;

        if (!Objects.equals(bind.getNickname(), oauthUserInfo.getNickname())) {
            bind.setNickname(oauthUserInfo.getNickname());
            updated = true;
        }

        if (!Objects.equals(bind.getAvatarUrl(), oauthUserInfo.getAvatarUrl())) {
            bind.setAvatarUrl(oauthUserInfo.getAvatarUrl());
            updated = true;
        }

        if (!Objects.equals(bind.getEmail(), oauthUserInfo.getEmail())) {
            bind.setEmail(oauthUserInfo.getEmail());
            updated = true;
        }

        if (updated) {
            bindDao.save(bind);
            log.info("更新第三方账号绑定信息：用户ID={}, 平台={}", bind.getUserId(), bind.getProviderType());
        }
    }

    /**
     * 生成安全的随机密码
     * 包含大小写字母、数字和特殊字符，长度24位
     *
     * @return 随机密码
     */

    private String generateSecureRandomPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?";

        String allChars = upperCase + lowerCase + digits + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(24);

        // 确保至少包含每种类型的字符
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // 填充剩余位数
        for (int i = 4; i < 24; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 打乱顺序
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }
}
