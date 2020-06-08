package com.limit.common.limiter.slidingwindow;

import com.limit.common.limiter.Limiter;
import com.limit.common.limiter.permitlimiter.PermitLimiter;
import com.limit.redis.service.RedisService;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlidingWindowLimiter implements Limiter {

    private static final Logger log = LoggerFactory.getLogger(PermitLimiter.class);

    private final RedisService redisService;

    private final RLock lock;

    private final long limit;

    private final long windowSize;

    private final long intervalNanos;

    // 构造函数
    public SlidingWindowLimiter(SlidingWindowLimiterConfig config) {
        this.redisService = config.getRedisService();
        this.lock = config.getLock();
        this.limit = config.getLimit();
        this.windowSize = config.getWindowSize();
        this.intervalNanos = config.getIntervalNanos();
    }

    /**
     * 放入新的（默认）窗口
     */
    public void putDefaultWindow() {

    }

    public Window getWindow() {
        return null;
    }

    public void setWindow() {

    }

    @Override
    public boolean acquire() {
        return false;
    }
}
