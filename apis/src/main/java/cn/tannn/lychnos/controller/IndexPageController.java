package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 前端路由控制器-合并打包要用
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/12 09:31
 */
@Controller
public class IndexPageController {

    /**
     * 处理已知的前端路由
     * 将这些路由请求直接转发到 index.html，避免 404 流程
     */
    @ApiMapping(value = {
            "/"
    }, method = RequestMethod.GET, checkToken = false)
    public String forwardToIndex() {
        return "forward:/index.html";
    }


    /**
     * 处理乱七八糟的请求,直接重定向到首页
     */
    @ApiMapping(value = {
            // ========== 浏览器和爬虫常见请求 ==========
            "/favicon.ico",           // 网站图标
            "/robots.txt",            // 爬虫协议文件
            "/sitemap.xml",           // 站点地图
            "/sitemap_index.xml",     // 站点地图索引
            "/manifest.json",         // PWA 清单文件
            "/browserconfig.xml",     // IE/Edge 浏览器配置

            // ========== Apple 设备相关 ==========
            "/apple-touch-icon.png",
            "/apple-touch-icon-precomposed.png",
            "/apple-app-site-association",  // iOS Universal Links

            // ========== 安全扫描和漏洞探测 ==========
            "/.env",                  // 环境变量文件(黑客常扫)
            "/.git/config",           // Git 配置(黑客常扫)
            "/.svn/entries",          // SVN 配置
            "/phpMyAdmin",            // 数据库管理工具
            "/phpmyadmin",
            "/admin",                 // 后台入口探测
            "/manager",               // Tomcat 管理页面
            "/console",               // 控制台
            "/actuator",              // Spring Boot Actuator(如果不需要)
            "/wp-admin",              // WordPress 后台
            "/wp-login.php",          // WordPress 登录

            // ========== 常见的配置文件探测 ==========
            "/web.config",            // IIS 配置
            "/config.php",
            "/configuration.php",
            "/.htaccess",
            "/composer.json",
            "/package.json",

            // ========== 备份文件探测 ==========
            "/backup.zip",
            "/backup.sql",
            "/database.sql",
            "/dump.sql",

            // ========== 其他常见请求 ==========
            "/crossdomain.xml",       // Flash 跨域策略
            "/ads.txt",               // 广告发布商验证
            "/humans.txt",            // 网站开发者信息
            "/security.txt",          // 安全联系方式
            "/.well-known/security.txt",

            // ========== 移动端相关 ==========
            "/app-ads.txt",
            "/assetlinks.json",       // Android App Links

            // ========== 常见的探测路径 ==========
            "/test",
            "/test.html",
            "/index.php",
            "/default.html",
            "/home.html"
            // 可以继续添加其他常见的"瞎访问"路径
    }, method = RequestMethod.GET, checkToken = false)
    public String redirectToHome() {
        return "redirect:/";  // 使用 redirect 而不是 forward
    }
}
