package com.limit.common.concurrent.ratelimiter;

import org.redisson.api.RLock;

public class RateLimiterConfig {

    /**
     * 令牌桶 key
     */
    private String key;

    /**
     * 每秒存入的令牌数
     */
    private Long permitsPerSecond;

    /**
     * 最大存储令牌数
     */
    private Long maxPermits;

    /**
     * 分布式互斥锁
     */
    private RLock lock;

    public RateLimiterConfig(String key, Long permitsPerSecond, Long maxPermits, RLock lock) {
        this.key = key;
        this.permitsPerSecond = permitsPerSecond;
        this.maxPermits = maxPermits;
        this.lock = lock;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getPermitsPerSecond() {
        return permitsPerSecond;
    }

    public void setPermitsPerSecond(Long permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
    }

    public Long getMaxPermits() {
        return maxPermits;
    }

    public void setMaxPermits(Long maxPermits) {
        this.maxPermits = maxPermits;
    }

    public RLock getLock() {
        return lock;
    }

    public void setLock(RLock lock) {
        this.lock = lock;
    }
}
