package com.limit.common.limiter.slidingwindow;

import com.limit.common.Constants;
import com.limit.redis.service.RedisService;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public class SlidingWindowLimiterConfig {

    private final String name;

    private final RedisService redisService;

    private final RLock lock;

    private final long limit;

    private final long windowSize;

    private final long intervalNanos;

    public SlidingWindowLimiterConfig(String name, RLock lock, RedisService redisService) {
        this(name, Constants.LIMIT_COUNT, Constants.WINDOW_SIZE, lock, redisService);
    }

    public SlidingWindowLimiterConfig(String name, long limit, long windowSize, RLock lock, RedisService redisService) {
        this.name = name;
        this.redisService = redisService;
        this.lock = lock;
        this.limit = limit;
        this.windowSize = windowSize;
        this.intervalNanos = TimeUnit.SECONDS.toNanos(1) / this.windowSize;
    }

    public String getName() {
        return name;
    }

    public RedisService getRedisService() {
        return redisService;
    }

    public RLock getLock() {
        return lock;
    }

    public long getLimit() {
        return limit;
    }

    public long getWindowSize() {
        return windowSize;
    }

    public long getIntervalNanos() {
        return intervalNanos;
    }
}
