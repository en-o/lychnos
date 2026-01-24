package cn.tannn.lychnos.controller;

import cn.tannn.jdevelops.annotations.web.authentication.ApiMapping;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * 前端路由控制器 & 安全防护拦截器
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/12 09:31
 */
@Slf4j
@Controller
public class IndexPageController {

    /**
     * 首页路由 - 转发到 index.html
     */
    @ApiMapping(value = {"/"}, method = RequestMethod.GET, checkToken = false)
    public String forwardToIndex() {
        return "forward:/index.html";
    }

    /**
     * 正常的浏览器请求 - 重定向到首页
     */
    @ApiMapping(value = {
            "/favicon.ico",
            "/robots.txt",
            "/sitemap.xml",
            "/manifest.json",
            "/apple-touch-icon.png",
            "/browserconfig.xml"
    }, method = RequestMethod.GET, checkToken = false)
    public String redirectToHome() {
        return "redirect:/";
    }

    /**
     * 恶意扫描和攻击路径拦截 - GET 请求
     * 直接返回 404,记录警告日志
     */
    @ApiMapping(value = {

            // ========== 版本控制系统配置 ==========
            "/.env",
            "/.env.local",
            "/.env.production",
            "/.env.development",
            "/.git/**",
            "/.git/config",
            "/.git/HEAD",
            "/.svn/**",
            "/.hg/**",
            "/.htaccess",

            // ========== 邮件服务器探测 ==========
            "/owa/**",
            "/owa/auth/x.js",
            "/Microsoft-Server-ActiveSync",
            "/EWS/**",

            // ========== 安全信息文件 ==========
            "/security.txt",
            "/.well-known/**",

            // ========== VPN/远程访问系统 ==========
            "/dana-na/**",
            "/remoteaccess/login",
            "/vpn/**",
            "/remote/**",
            "/citrix/**",
            "/rdp/**",

            // ========== PHP 配置文件 ==========
            "/config.php",
            "/config.inc.php",
            "/wp-config.php",
            "/configuration.php",
            "/database.php",
            "/db.php",
            "/connect.php",
            "/settings.php",
            "/local.php",

            // ========== Python 配置文件 ==========
            "/config.py",
            "/settings.py",
            "/local_settings.py",
            "/production.py",
            "/development.py",
            "/manage.py",

            // ========== Ruby 配置文件 ==========
            "/config.rb",
            "/database.yml",
            "/secrets.yml",
            "/credentials.yml.enc",
            "/config/database.yml",
            "/config/secrets.yml",

            // ========== Java/Spring 配置文件 ==========
            "/application.properties",
            "/application.yml",
            "/application-prod.yml",
            "/application-dev.yml",
            "/config.properties",
            "/database.properties",
            "/hibernate.cfg.xml",
            "/persistence.xml",
            "/applicationContext.xml",
            "/spring-*.xml",

            // ========== .NET 配置文件 ==========
            "/web.config",
            "/Web.config",
            "/app.config",
            "/appsettings.json",
            "/appsettings.Production.json",
            "/appsettings.Development.json",

            // ========== 数据库管理工具 ==========
            "/phpMyAdmin/**",
            "/phpmyadmin/**",
            "/pma/**",
            "/adminer/**",
            "/adminer.php",
            "/mysql/**",

            // ========== 后台/管理入口 ==========
            "/admin/**",
            "/administrator/**",
            "/manager/**",
            "/console/**",
            "/backend/**",
            "/control/**",
            "/manage/**",

            // ========== WordPress 探测 ==========
            "/wp-admin/**",
            "/wp-login.php",
            "/wp-content/**",
            "/wp-includes/**",
            "/xmlrpc.php",

            // ========== Swagger/API 文档 ==========
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/v3/api-docs/**",
            "/doc.html",
            "/api-docs/**",
            "/openapi.*",
            "/swagger.*",

            // ========== Spring Boot Actuator ==========
            "/actuator",
            "/actuator/**",
            "/health",
            "/metrics",
            "/env",
            "/trace",
            "/dump",
            "/heapdump",
            "/threaddump",
            "/jolokia/**",

            // ========== 监控工具 ==========
            "/druid/**",
            "/monitoring/**",
            "/grafana/**",
            "/prometheus/**",

            // ========== GraphQL ==========
            "/graphql",
            "/graphiql",
            "/playground",

            // ========== DNS/网络探测 ==========
            "/dns-query",
            "/dns/**",

            // ========== SDK/开发工具路径 ==========
            "/SDK/**",
            "/developmentserver/**",
            "/api/debug/**",
            "/debug/**",

            // ========== 前端框架/UI 组件探测 ==========
            "/theme/**",
            "/assets/umi.js",
            "/umi.js",
            "/layui/**",

            // ========== 特定应用/系统路径 ==========
            "/pscc/**",
            "/start/index.html",
            "/cc/start/**",

            // ========== 备份文件 ==========
            "/backup/**",
            "/backup.*",
            "/backup.zip",
            "/backup.sql",
            "/backup.tar.gz",
            "/dump.*",
            "/database.*",
            "/db_backup.*",
            "/*.bak",
            "/*.backup",
            "/*.old",
            "/*.sql",

            // ========== 测试/临时文件 ==========
            "/test/**",
            "/test.php",
            "/test.jsp",
            "/temp/**",
            "/tmp/**",
            "/phpinfo.php",
            "/info.php",

            // ========== 容器/编排工具 ==========
            "/docker/**",
            "/kubernetes/**",
            "/k8s/**",

            // ========== CI/CD 工具 ==========
            "/jenkins/**",
            "/gitlab/**",
            "/travis.yml",
            "/.gitlab-ci.yml",
            "/Jenkinsfile",

            // ========== 日志文件 ==========
            "/logs/**",
            "/log/**",
            "/*.log",
            "/error.log",
            "/access.log",
            "/debug.log",

            // ========== 恶意脚本文件 ==========
            "/shell.php",
            "/cmd.php",
            "/eval.php",
            "/upload.php",
            "/uploader.php",
            "/filemanager/**",
            "/FCKeditor/**",
            "/ckeditor/**",
            "/kindeditor/**"

    }, method = RequestMethod.GET, checkToken = false)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void blockMaliciousGetRequests(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.warn("🚨 恶意扫描已拦截 | Method: GET | URI: {} | IP: {} | UA: {}",
                uri, ip, userAgent);
    }

    /**
     * 恶意扫描和攻击路径拦截 - POST 请求
     */
    @ApiMapping(value = {
            "/owa/**",
            "/dana-na/**",
            "/.env",
            "/phpMyAdmin/**",
            "/admin/**",
            "/upload.php",
            "/shell.php",
            "/cmd.php",
            "/eval.php"
    }, method = RequestMethod.POST, checkToken = false)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void blockMaliciousPostRequests(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.warn("🚨 恶意攻击已拦截 | Method: POST | URI: {} | IP: {} | UA: {}",
                uri, ip, userAgent);
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个 IP 的情况,取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
