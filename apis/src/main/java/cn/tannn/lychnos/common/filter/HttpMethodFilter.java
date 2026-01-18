package cn.tannn.lychnos.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * HTTP 方法过滤器
 * 用于拦截非标准的 HTTP 方法（如 WebDAV 的 PROPFIND、MKCOL 等）
 * 防止恶意扫描和非法请求
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/18
 */
@Component
@Order(1)
@Slf4j
public class HttpMethodFilter implements Filter {

    /**
     * 允许的 HTTP 方法列表
     */
    private static final Set<String> ALLOWED_METHODS = Set.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
    );

    /**
     * 不需要记录日志的方法（太常见的扫描方法）
     */
    private static final Set<String> SILENT_REJECT_METHODS = Set.of(
            "PROPFIND", "PROPPATCH", "MKCOL", "COPY", "MOVE", "LOCK", "UNLOCK",
            "TRACE", "CONNECT"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String method = request.getMethod();

        // 检查是否为允许的方法
        if (!ALLOWED_METHODS.contains(method)) {
            String requestURI = request.getRequestURI();
            String clientIP = getClientIP(request);

            // 对于常见的扫描方法，使用 DEBUG 级别，避免日志污染
            if (SILENT_REJECT_METHODS.contains(method)) {
                log.debug("Rejected unsupported HTTP method: {} from IP: {} for URI: {}",
                        method, clientIP, requestURI);
            } else {
                // 对于其他非标准方法，记录 WARN 级别
                log.warn("Rejected unusual HTTP method: {} from IP: {} for URI: {}",
                        method, clientIP, requestURI);
            }

            // 返回 405 Method Not Allowed
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                    "{\"error\":\"Method Not Allowed\",\"message\":\"HTTP method '%s' is not supported\"}",
                    method
            ));
            return;
        }

        // 继续执行过滤器链
        chain.doFilter(request, response);
    }

    /**
     * 获取客户端真实 IP 地址
     * 考虑反向代理的情况
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
