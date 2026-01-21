package cn.tannn.lychnos.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.util.List;

/**
 * 智能代理配置 - 自动检测系统代理
 *
 * @author tan
 * @date 2026/1/21
 */
@Configuration
@Slf4j
public class SmartProxyConfig {

    @Value("${app.proxy.enabled:false}")
    private boolean manualProxyEnabled;

    @Value("${app.proxy.host:}")
    private String manualProxyHost;

    @Value("${app.proxy.port:0}")
    private int manualProxyPort;

    @Value("${app.proxy.read-timeout:600000}")
    private int readTimeout;

    @Value("${app.proxy.connect-timeout:600000}")
    private int connectTimeout;


    @Bean
    public RestTemplate restTemplate() {

        // 启用系统代理
        System.setProperty("java.net.useSystemProxies", "true");
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        Proxy proxy = null;
        // 优先使用手动配置
        if (manualProxyEnabled && manualProxyHost != null && !manualProxyHost.isEmpty()) {
            proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(manualProxyHost, manualProxyPort));
            log.info("✓ 使用手动配置代理: {}:{}", manualProxyHost, manualProxyPort);
        } else {
            // 自动检测
            proxy = detectProxy();
        }
        if (proxy != null && proxy.type() != Proxy.Type.DIRECT) {
            factory.setProxy(proxy);
        } else {
            log.warn("✗ 未检测到代理，访问 GitHub/LinuxDo 可能失败");
        }
        return new RestTemplate(factory);
    }

    private Proxy detectProxy() {
        try {
            // 使用 ProxySelector 自动检测
            ProxySelector selector = ProxySelector.getDefault();
            if (selector != null) {
                List<Proxy> proxies = selector.select(new URI("https://github.com"));
                for (Proxy proxy : proxies) {
                    if (proxy.type() != Proxy.Type.DIRECT) {
                        return proxy;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("检测代理失败", e);
        }
        return Proxy.NO_PROXY;
    }
}
