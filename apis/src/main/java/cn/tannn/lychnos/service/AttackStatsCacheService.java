package cn.tannn.lychnos.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 恶意攻击统计缓存服务
 * <p>使用 Guava Cache 管理攻击统计数据，每小时自动清理和上报</p>
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/24
 */
@Service
@Slf4j
public class AttackStatsCacheService {

    /**
     * 攻击统计缓存
     * Key: 攻击者IP
     * Value: 攻击次数
     * 有效期: 1小时（与统计周期一致）
     */
    private Cache<String, Long> attackStatsCache;

    /**
     * 初始化缓存
     */
    @PostConstruct
    public void init() {
        log.info("初始化攻击统计缓存，统计周期: 1小时");

        attackStatsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)  // 1小时后自动过期
                .maximumSize(1000)  // 最多缓存1000个IP
                .build();
    }

    /**
     * 记录一次攻击
     *
     * @param ip 攻击者IP
     * @param uri 攻击路径
     * @param method 请求方法
     * @param userAgent 用户代理
     */
    public void recordAttack(String ip, String uri, String method, String userAgent) {
        if (ip == null || ip.isEmpty()) {
            log.warn("尝试记录空IP的攻击");
            return;
        }

        // 获取当前攻击次数并+1
        Long currentCount = attackStatsCache.getIfPresent(ip);
        long newCount = (currentCount == null ? 0 : currentCount) + 1;
        attackStatsCache.put(ip, newCount);

        // 检查是否为高频攻击
        if (isHighFrequencyAttacker(ip, 50)) {  // 1小时内超过50次
            log.error("⚠️ 检测到高频攻击者! IP: {} | 累计: {} 次 | 建议加入黑名单",
                    ip, newCount);
            // TODO: 可以在这里触发自动封禁IP的逻辑
        }

        // 记录警告日志
        log.warn("🚨 恶意{}请求已拦截 | Method: {} | URI: {} | IP: {} | 累计攻击: {} 次 | UA: {}",
                method.equals("POST") ? "攻击" : "扫描",
                method, uri, ip, newCount, userAgent);
    }

    /**
     * 检查IP是否为高频攻击者
     *
     * @param ip 攻击者IP
     * @param threshold 阈值
     * @return true-是高频攻击，false-否
     */
    public boolean isHighFrequencyAttacker(String ip, int threshold) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        Long count = attackStatsCache.getIfPresent(ip);
        return count != null && count >= threshold;
    }

    /**
     * 获取IP的攻击次数
     *
     * @param ip 攻击者IP
     * @return 攻击次数
     */
    public long getAttackCount(String ip) {
        if (ip == null || ip.isEmpty()) {
            return 0;
        }
        Long count = attackStatsCache.getIfPresent(ip);
        return count == null ? 0 : count;
    }

    /**
     * 每小时统计并清理攻击数据
     * 每小时的整点执行（如：14:00:00, 15:00:00）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void reportAndClearStats() {
        Map<String, Long> stats = attackStatsCache.asMap();

        if (stats.isEmpty()) {
            log.info("过去1小时没有检测到恶意攻击");
            return;
        }

        log.warn("==================== 过去1小时攻击统计 ====================");
        log.warn("总攻击IP数: {} 个", stats.size());

        // 按攻击次数降序排序，输出前10名
        stats.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(entry ->
                        log.warn("攻击 IP: {} | 攻击次数: {} 次",
                                entry.getKey(), entry.getValue())
                );

        // 统计总攻击次数
        long totalAttacks = stats.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        log.warn("总攻击次数: {} 次", totalAttacks);
        log.warn("========================================================");

        // 清空缓存，开始新的统计周期
        attackStatsCache.invalidateAll();
        log.info("攻击统计已清空，开始新的统计周期");
    }

    /**
     * 获取当前缓存中的攻击IP数量
     *
     * @return IP数量
     */
    public long getAttackerCount() {
        return attackStatsCache.size();
    }

    /**
     * 清空所有统计数据（仅用于测试或管理维护）
     */
    public void clearAll() {
        attackStatsCache.invalidateAll();
        log.warn("所有攻击统计数据已清空");
    }
}
