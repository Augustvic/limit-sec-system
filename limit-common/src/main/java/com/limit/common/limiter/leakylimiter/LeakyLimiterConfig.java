package com.limit.common.limiter.leakylimiter;

import com.limit.common.Constants;
import com.limit.redis.service.RedisService;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public class LeakyLimiterConfig {

    private final RedisService redisService;

    // 漏桶唯一标识
    private final String name;

    // 分布式互斥锁
    private final RLock lock;

    // 每两滴水之间的时间间隔
    private final long intervalNanos;

    // 每秒钟最多通过多少滴水
    private final long leakPerSecond;

    public LeakyLimiterConfig(String name, RLock lock, RedisService redisService) {
        this(name, Constants.LEAK_PER_SECOND, lock, redisService);
    }

    public LeakyLimiterConfig(String name, long leakPerSecond, RLock lock, RedisService redisService) {
        this.name = name;
        this.leakPerSecond = leakPerSecond;
        this.intervalNanos = TimeUnit.SECONDS.toNanos(1) / leakPerSecond;
        this.lock = lock;
        this.redisService = redisService;
    }

    public RedisService getRedisService() {
        return redisService;
    }

    public String getName() {
        return name;
    }

    public RLock getLock() {
        return lock;
    }

    public long getIntervalNanos() {
        return intervalNanos;
    }

    public long getLeakPerSecond() {
        return leakPerSecond;
    }
}
