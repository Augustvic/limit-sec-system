package com.limit.common.concurrent.ratelimiter;

import com.limit.redis.service.RedisService;
import org.redisson.api.RLock;

public class RateLimiterConfig {

    private RedisService redisService;

    /**
     * 令牌桶 key
     */
    private final String key;

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

    public RateLimiterConfig(String key, long permitsPerSecond, long maxPermits, RLock lock, RedisService redisService) {
        this.key = key;
        this.permitsPerSecond = permitsPerSecond;
        this.maxPermits = maxPermits;
        this.lock = lock;
        this.redisService = redisService;
    }

    public RedisService getRedisService() {
        return redisService;
    }

    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    public String getKey() {
        return key;
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
}
