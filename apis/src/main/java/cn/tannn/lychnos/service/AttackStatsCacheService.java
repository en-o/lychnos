package cn.tannn.lychnos.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 恶意攻击统计缓存服务
 * <p>使用 Guava Cache 管理攻击统计数据，1小时自动过期</p>
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
     * 有效期: 1小时（自动清理）
     */
    private Cache<String, Long> attackStatsCache;

    /**
     * 初始化缓存
     */
    @PostConstruct
    public void init() {
        log.info("初始化攻击统计缓存，数据保留时长: 1小时");

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

        // 记录警告日志
        log.warn("🚨 恶意{}请求已拦截 | Method: {} | URI: {} | IP: {} | 累计: {} 次 | UA: {}",
                method.equals("POST") ? "攻击" : "扫描",
                method, uri, ip, newCount, userAgent);

        // 高频攻击告警
        if (newCount >= 50) {
            log.error("⚠️ 检测到高频攻击者! IP: {} | 累计: {} 次 | 建议加入黑名单", ip, newCount);
        }
    }

    /**
     * 获取攻击统计数据（按攻击次数降序）
     *
     * @param limit 返回数量限制，0表示返回全部
     * @return 攻击统计 Map (IP -> 攻击次数)，已按次数降序排序
     */
    public Map<String, Long> getAttackStats(int limit) {
        Map<String, Long> stats = attackStatsCache.asMap();

        if (stats.isEmpty()) {
            return new LinkedHashMap<>();
        }

        // 按攻击次数降序排序
        var sortedStream = stats.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        // 如果有限制，取前N个
        if (limit > 0) {
            sortedStream = sortedStream.limit(limit);
        }

        return sortedStream.collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
        ));
    }

    /**
     * 获取总攻击次数
     *
     * @return 总攻击次数
     */
    public long getTotalAttackCount() {
        return attackStatsCache.asMap().values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    /**
     * 获取攻击IP总数
     *
     * @return IP数量
     */
    public long getAttackerCount() {
        return attackStatsCache.size();
    }

    /**
     * 获取单个IP的攻击次数
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
     * 检查IP是否为高频攻击者
     *
     * @param ip 攻击者IP
     * @param threshold 阈值
     * @return true-是高频攻击，false-否
     */
    public boolean isHighFrequencyAttacker(String ip, int threshold) {
        return getAttackCount(ip) >= threshold;
    }

    /**
     * 清空所有统计数据（管理员操作）
     */
    public void clearAll() {
        long beforeSize = attackStatsCache.size();
        attackStatsCache.invalidateAll();
        log.warn("所有攻击统计数据已清空，共清除 {} 条记录", beforeSize);
    }

    /**
     * 移除指定IP的统计（管理员操作）
     *
     * @param ip 攻击者IP
     */
    public void removeIp(String ip) {
        Long count = attackStatsCache.getIfPresent(ip);
        if (count != null) {
            attackStatsCache.invalidate(ip);
            log.info("已移除IP {} 的攻击统计，原攻击次数: {}", ip, count);
        }
    }
}
