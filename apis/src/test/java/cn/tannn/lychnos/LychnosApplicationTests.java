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
     * </p>
     */
//    @Test
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
     * </p>
     */
//    @Test
    void addLinuxDoConfig() {
        // 1. 准备配置数据 (请在此处填入真实的 Client ID 和 Client Secret)
        String clientId = "your_linuxdo_client_id";
        String clientSecret = "your_linuxdo_client_secret";

        if (clientId.contains("your_")) {
            System.err.println("❌ 请先在 addLinuxDoConfig 方法中填写有效的 LinuxDo ClientId 和 ClientSecret");
            return;
        }

        OAuthConfig config = new OAuthConfig();
        config.setProviderType(ProviderType.LINUXDO);
        config.setClientId(clientId);
        config.setClientSecret(clientSecret);
        // 注意：请根据 LinuxDo 官方文档确认最新的 OAuth2 端点
        config.setAuthorizeUrl("https://connect.linux.do/oauth2/authorize");
        config.setTokenUrl("https://connect.linux.do/oauth2/token");
        config.setUserInfoUrl("https://connect.linux.do/api/user");
        config.setScope("read");
        config.setIconUrl("https://linux.do/uploads/default/original/3X/9/d/9dd497313d118893779d729a43a75e3c79212.png"); // 示例图标
        config.setSortOrder(2);

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
//    @Test
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
     * 列出所有配置
     */
    @Test
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
        }
        System.out.println("--------------------------------------------------\n");
    }
}
