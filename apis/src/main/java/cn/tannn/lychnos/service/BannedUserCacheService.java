package cn.tannn.lychnos.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 封禁用户缓存服务
 * <p>使用Guava Cache管理封禁用户ID，有效期1天</p>
 * <p>由于JWT无状态，需要通过缓存来实时控制封禁用户的访问权限</p>
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/24
 */
@Service
@Slf4j
public class BannedUserCacheService {

    /**
     * 封禁用户缓存
     * Key: 用户ID
     * Value: 封禁时间戳（用于日志记录）
     * 有效期: 1天（自动过期兜底）
     */
    private final Cache<Long, Long> bannedUserCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(10000)
            .build();

    /**
     * 添加封禁用户到缓存
     *
     * @param userId 用户ID
     */
    public void addBannedUser(Long userId) {
        if (userId == null) {
            log.warn("尝试添加空用户ID到封禁缓存");
            return;
        }
        bannedUserCache.put(userId, System.currentTimeMillis());
        log.info("用户已添加到封禁缓存，userId: {}", userId);
    }

    /**
     * 从缓存中移除封禁用户
     *
     * @param userId 用户ID
     */
    public void removeBannedUser(Long userId) {
        if (userId == null) {
            log.warn("尝试移除空用户ID从封禁缓存");
            return;
        }
        bannedUserCache.invalidate(userId);
        log.info("用户已从封禁缓存中移除，userId: {}", userId);
    }

    /**
     * 检查用户是否被封禁
     *
     * @param userId 用户ID
     * @return true-已封禁，false-未封禁
     */
    public boolean isBanned(Long userId) {
        if (userId == null) {
            return false;
        }
        Long bannedTime = bannedUserCache.getIfPresent(userId);
        return bannedTime != null;
    }

    /**
     * 清空所有封禁缓存（仅用于测试或管理维护）
     */
    public void clearAll() {
        bannedUserCache.invalidateAll();
        log.warn("所有封禁用户缓存已清空");
    }

    /**
     * 获取当前缓存中的封禁用户数量
     *
     * @return 封禁用户数量
     */
    public long getBannedUserCount() {
        return bannedUserCache.size();
    }
}
