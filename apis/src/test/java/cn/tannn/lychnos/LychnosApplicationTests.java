package cn.tannn.lychnos;

import cn.tannn.lychnos.entity.OAuthConfig;
import cn.tannn.lychnos.enums.ProviderType;
import cn.tannn.lychnos.service.OAuthConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
class LychnosApplicationTests {

    @Autowired
    private OAuthConfigService oauthConfigService;

    @Test
    void contextLoads() {
    }

    /**
     * 添加或更新 GitHub 配置
     * <p>
     * 请替换 "your_xxx" 为实际值后运行
     * https://github.com/settings/applications/3348764
     * </p>
     */
    // @Test
    void addGithubConfig() {
        // 1. 准备配置数据 (请在此处填入真实的 Client ID 和 Client Secret)
        String clientId = "your_github_client_id";
        String clientSecret = "your_github_client_secret";

        // 简单的检查，防止提交了占位符
        if (clientId.contains("your_")) {
            System.err.println("❌ 请先在 addGithubConfig 方法中填写有效的 GitHub ClientId 和 ClientSecret");
            return;
        }

        OAuthConfig config = new OAuthConfig();
        config.setProviderType(ProviderType.GITHUB);
        config.setClientId(clientId);
        config.setClientSecret(clientSecret);
        config.setAuthorizeUrl("https://github.com/login/oauth/authorize");
        config.setTokenUrl("https://github.com/login/oauth/access_token");
        config.setUserInfoUrl("https://api.github.com/user");
        config.setScope("read:user user:email");
        config.setIconUrl("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png");
        config.setSortOrder(1);
        config.setEnabled(true);

        // Web回调地址前缀配置说明：
        // - 此字段只需填写域名+路径前缀，后端会自动拼接 #/oauth/callback
        // - 示例1：http://localhost:3000/lychnos  -> 最终URL: http://localhost:3000/lychnos#/oauth/callback?token=xxx
        // - 示例2：http://localhost:3000/         -> 最终URL: http://localhost:3000#/oauth/callback?token=xxx
        // - 示例3：http://localhost:3000          -> 最终URL: http://localhost:3000#/oauth/callback?token=xxx
        // - 示例4：https://example.com            -> 最终URL: https://example.com#/oauth/callback?token=xxx
        // - 可以为空（相对路径）：""               -> 最终URL: #/oauth/callback?token=xxx
        // - 注意：末尾的斜杠会被自动移除，#/oauth/callback 是固定路由不可修改
        config.setWebCallbackUrl("http://localhost:5173");

        // 2. 检查是否存在，存在则更新
        Optional<OAuthConfig> existing = oauthConfigService.getConfigByType(ProviderType.GITHUB);
        if (existing.isPresent()) {
            System.out.println("🔄 GitHub 配置已存在，正在更新...");
            config.setId(existing.get().getId());
        } else {
            System.out.println("➕ GitHub 配置不存在，正在创建...");
        }

        // 3. 保存
        oauthConfigService.saveConfig(config);
        System.out.println("✅ GitHub 配置保存成功！");
    }

    /**
     * 添加或更新 LinuxDo 配置
     * <p>
     * 请替换 "your_xxx" 为实际值后运行
     * https://connect.linux.do/dash/sso
     * </p>
     */
    // @Test
    void addLinuxDoConfig() {
        // 1. 准备配置数据 (请在此处填入真实的 Client ID 和 Client Secret)
        String clientId = "your_linuxdo_client_id";
        String clientSecret = "your_linuxdo_client_secret";

        if (clientId.contains("your_")) {
            System.err.println("❌ 请先在 addLinuxDoConfig 方法中填写有效的 LinuxDo ClientId 和 ClientSecret");
            return;
        }

        // https://connect.linux.do
        // https://connect.linuxdo.org/
        OAuthConfig config = new OAuthConfig();
        config.setProviderType(ProviderType.LINUXDO);
        config.setClientId(clientId);
        config.setClientSecret(clientSecret);
        // 注意：请根据 LinuxDo 官方文档确认最新的 OAuth2 端点
        config.setAuthorizeUrl("https://connect.linux.do/oauth2/authorize");
        config.setTokenUrl("https://connect.linux.do/oauth2/token");
        config.setUserInfoUrl("https://connect.linux.do/api/user");
        config.setScope("read:user user:email");
        config.setIconUrl(
                "https://linux.do/uploads/default/optimized/4X/c/c/d/ccd8c210609d498cbeb3d5201d4c259348447562_2_32x32.png"); // 示例图标
        config.setSortOrder(2);
        config.setEnabled(true);

        // Web回调地址前缀配置说明：
        // - 此字段只需填写域名+路径前缀，后端会自动拼接 #/oauth/callback
        // - 示例1：http://localhost:3000/lychnos  -> 最终URL: http://localhost:3000/lychnos#/oauth/callback?token=xxx
        // - 示例2：http://localhost:3000/         -> 最终URL: http://localhost:3000#/oauth/callback?token=xxx
        // - 示例3：http://localhost:3000          -> 最终URL: http://localhost:3000#/oauth/callback?token=xxx
        // - 示例4：https://example.com            -> 最终URL: https://example.com#/oauth/callback?token=xxx
        // - 可以为空（相对路径）：""               -> 最终URL: #/oauth/callback?token=xxx
        // - 注意：末尾的斜杠会被自动移除，#/oauth/callback 是固定路由不可修改
        config.setWebCallbackUrl("http://localhost:5173");

        // 2. 检查是否存在
        Optional<OAuthConfig> existing = oauthConfigService.getConfigByType(ProviderType.LINUXDO);
        if (existing.isPresent()) {
            System.out.println("🔄 LinuxDo 配置已存在，正在更新...");
            config.setId(existing.get().getId());
        } else {
            System.out.println("➕ LinuxDo 配置不存在，正在创建...");
        }

        // 3. 保存
        oauthConfigService.saveConfig(config);
        System.out.println("✅ LinuxDo 配置保存成功！");
    }

    /**
     * 删除指定平台的配置
     * <p>
     * 修改方法内的 typeToDelete 变量来指定要删除的平台
     * </p>
     */
    // @Test
    void deleteConfig() {
        // 修改这里来指定要删除的平台，例如 ProviderType.GITHUB
        ProviderType typeToDelete = null;

        if (typeToDelete == null) {
            System.err.println("⚠️ 请先在 deleteConfig 方法中指定要删除的 ProviderType (typeToDelete)");
            return;
        }

        Optional<OAuthConfig> existing = oauthConfigService.getConfigByType(typeToDelete);
        if (existing.isPresent()) {
            oauthConfigService.getJpaBasicsDao().deleteById(existing.get().getId());
            System.out.println("🗑️ 已删除配置: " + typeToDelete);
        } else {
            System.out.println("⚠️ 未找到配置: " + typeToDelete);
        }
    }

    /**
     * 根据 ID 更新配置的 ClientId 和 ClientSecret
     * <p>
     * 请替换 updateId, newClientId, newClientSecret 为实际值后运行
     * </p>
     */
     @Test
    void updateConfigById() {
        // 1. 准备更新数据
        Long updateId = null; // 替换为要更新的配置ID，例如 1L
        String newClientId = "your_new_client_id";
        String newClientSecret = "your_new_client_secret";

        if (updateId == null || newClientId.contains("your_")) {
            System.err.println("❌ 请先在 updateConfigById 方法中填写有效的 ID, ClientId 和 ClientSecret");
            return;
        }

        // 2. 查找配置
        Optional<OAuthConfig> optional = oauthConfigService.getJpaBasicsDao().findById(updateId);
        if (optional.isPresent()) {
            OAuthConfig config = optional.get();
            System.out.println("🔄 找到配置: " + config.getProviderType() + " (ID: " + config.getId() + ")");
            System.out.println("   旧 ClientId: " + config.getClientId());

            // 3. 更新字段
            config.setClientId(newClientId);
            config.setClientSecret(newClientSecret);

            // 4. 保存 (会自动触发 AttributeConverter 加密)
            oauthConfigService.saveConfig(config);
            System.out.println("✅ 配置更新并加密保存成功！");
        } else {
            System.err.println("❌ 未找到 ID 为 " + updateId + " 的配置");
        }
    }

    /**
     * 更新所有配置的 webCallbackUrl
     * <p>
     * 用于为已有的 OAuth 配置批量添加 webCallbackUrl 字段
     * </p>
     */
    // @Test
    void updateWebCallbackUrl() {
        List<OAuthConfig> configs = oauthConfigService.getAllConfigs();

        if (configs.isEmpty()) {
            System.err.println("⚠️ 没有找到任何 OAuth 配置");
            return;
        }

        System.out.println("🔄 开始更新 webCallbackUrl...");

        for (OAuthConfig config : configs) {
            // Web回调地址前缀配置说明：
            // - 此字段只需填写域名+路径前缀，后端会自动拼接 #/oauth/callback
            // - 示例1：http://localhost:3000/lychnos  -> 最终URL: http://localhost:3000/lychnos#/oauth/callback?token=xxx
            // - 示例2：http://localhost:3000/         -> 最终URL: http://localhost:3000#/oauth/callback?token=xxx
            // - 示例3：http://localhost:3000          -> 最终URL: http://localhost:3000#/oauth/callback?token=xxx
            // - 可以为空（相对路径）：""               -> 最终URL: #/oauth/callback?token=xxx
            // - 注意：末尾的斜杠会被自动移除，#/oauth/callback 是固定路由不可修改
            config.setWebCallbackUrl("http://localhost:5173");
            oauthConfigService.saveConfig(config);
            System.out.println("✅ 已更新 " + config.getProviderType() + " 的 webCallbackUrl");
        }

        System.out.println("✅ 所有配置更新完成！");
    }

    /**
     * 列出所有配置
     */
//    @Test
    void listConfigs() {
        List<OAuthConfig> configs = oauthConfigService.getAllConfigs();
        System.out.println("\n📋 当前 OAuth2 配置列表 (" + configs.size() + "):");
        for (OAuthConfig config : configs) {
            System.out.println("--------------------------------------------------");
            System.out.println("ID: " + config.getId());
            System.out.println("Type: " + config.getProviderType());
            System.out.println("Name: " + config.getProviderType().getDisplayName());
            // 注意：如果配置正确，这里打印的 clientId 应该是解密后的明文
            System.out.println("ClientId: " + config.getClientId());
            System.out.println("AuthUrl: " + config.getAuthorizeUrl());
            System.out.println("WebCallbackUrl: " + config.getWebCallbackUrl());
            System.out.println("Enabled: " + config.getEnabled());
        }
        System.out.println("--------------------------------------------------\n");
    }
}
