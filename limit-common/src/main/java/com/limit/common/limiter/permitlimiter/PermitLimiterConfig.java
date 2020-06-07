package com.limit.common.limiter.permitlimiter;

import com.limit.common.Constants;
import com.limit.redis.service.RedisService;
import org.redisson.api.RLock;

public class PermitLimiterConfig {

    private final RedisService redisService;

    private final String name;

    /**
     * 每秒存入的令牌数
     */
    private final long permitsPerSecond;

    /**
     * 最大存储令牌数
     */
    private final long maxPermits;

    /**
     * 分布式互斥锁
     */
    private final RLock lock;

    public PermitLimiterConfig(String name, RLock lock, RedisService redisService) {
        this(name, Constants.PERMITS_PER_SECOND, Constants.MAX_PERMITS, lock, redisService);
    }

    public PermitLimiterConfig(String name, long permitsPerSecond, long maxPermits, RLock lock, RedisService redisService) {
        this.name = name;
        this.permitsPerSecond = permitsPerSecond;
        this.maxPermits = maxPermits;
        this.lock = lock;
        this.redisService = redisService;
    }

    public RedisService getRedisService() {
        return redisService;
    }

    public long getPermitsPerSecond() {
        return permitsPerSecond;
    }

    public long getMaxPermits() {
        return maxPermits;
    }

    public RLock getLock() {
        return lock;
    }

    public String getName() {
        return name;
    }
}
