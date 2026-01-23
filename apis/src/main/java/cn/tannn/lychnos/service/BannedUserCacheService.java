package cn.tannn.lychnos.service;

import cn.tannn.jdevelops.utils.jwt.config.JwtConfig;
import cn.tannn.lychnos.common.constant.UserStatus;
import cn.tannn.lychnos.dao.UserInfoDao;
import cn.tannn.lychnos.entity.UserInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 封禁用户缓存服务
 * <p>使用Guava Cache管理封禁用户ID，有效期与JWT过期时间一致</p>
 * <p>由于JWT无状态，需要通过缓存来实时控制封禁用户的访问权限</p>
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/24
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BannedUserCacheService {

    private final JwtConfig jwtConfig;
    private final UserInfoDao userInfoDao;

    /**
     * 封禁用户缓存
     * Key: 用户ID
     * Value: 封禁时间戳（用于日志记录）
     * 有效期: 与JWT过期时间一致（自动过期兜底）
     */
    private Cache<Long, Long> bannedUserCache;

    /**
     * 初始化缓存并加载所有被封禁的用户
     * 在Spring容器启动后自动执行
     */
    @PostConstruct
    public void init() {
        // 获取JWT过期时间[默认  过期时间为一天 (单位:小时)]
        long expireTimeMs = jwtConfig.getExpireTime();
        log.info("初始化封禁用户缓存，过期时间: {}ms ({}小时)", expireTimeMs, expireTimeMs / 1000 / 3600);

        // 初始化缓存
        bannedUserCache = CacheBuilder.newBuilder()
                .expireAfterWrite(expireTimeMs, TimeUnit.MILLISECONDS)
                .maximumSize(10000)
                .build();

        // 从数据库加载所有被封禁的用户
        loadBannedUsersFromDatabase();
    }

    /**
     * 从数据库加载所有被封禁的用户到缓存
     * 防止项目重启后封禁状态丢失
     */
    private void loadBannedUsersFromDatabase() {
        try {
            List<UserInfo> bannedUsers = userInfoDao.findByStatus(UserStatus.BANNED.getCode());
            long currentTime = System.currentTimeMillis();

            for (UserInfo user : bannedUsers) {
                bannedUserCache.put(user.getId(), currentTime);
            }

            log.info("从数据库加载了 {} 个被封禁用户到缓存", bannedUsers.size());
        } catch (Exception e) {
            log.error("从数据库加载封禁用户失败", e);
        }
    }

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
