package com.limit.common.limiter.leakylimiter;

import com.limit.common.limiter.Limiter;
import com.limit.redis.key.common.LeakyBucketKey;
import com.limit.redis.service.RedisService;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 无容量漏桶
 */
public class LeakyLimiter implements Limiter {

    private static final Logger log = LoggerFactory.getLogger(LeakyLimiter.class);

    private final RedisService redisService;

    // 漏桶唯一标识
    private final String name;

    // 分布式互斥锁
    private final RLock lock;

    // 每两滴水之间的时间间隔
    private final long intervalNanos;

    // 每秒钟最多通过多少滴水
    private final long leakPerSecond;

    public LeakyLimiter(LeakyLimiterConfig config) {
        this.name = config.getName();
        this.leakPerSecond = config.getLeakPerSecond();
        this.intervalNanos = config.getIntervalNanos();
        this.lock = config.getLock();
        this.redisService = config.getRedisService();
    }

    /**
     * 尝试获取锁
     * @return 获取成功返回 true
     */
    private boolean lock() {
        try {
            // 等待 100 秒，获得锁 100 秒后自动解锁
            return this.lock.tryLock(100, 100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 释放锁
     */
    private void unlock() {
        this.lock.unlock();
    }

    /**
     * 尝试通过漏桶
     *
     * @return 获取成功返回 true，失败返回 false
     */
    @Override
    public boolean acquire() {
        while (true) {
            if (lock()) {
                try {
                    return tryAcquire();
                } finally {
                    unlock();
                }
            }
        }
    }

    private boolean tryAcquire() {
        long recent = getRecent();
        long now = System.nanoTime();
        if (now - recent >= this.intervalNanos) {
            resync(now);
            return true;
        } else {
            log.info("Acquire LeakyLimiter[" + this.name + "] failed.");
            return false;
        }
    }

    private long getRecent() {
        Long recent = redisService.get(LeakyBucketKey.leakyBucket, this.name, Long.class);
        if (recent == null) {
            recent = System.nanoTime();
            resync(recent);
            return recent - intervalNanos;
        }
        return recent;
    }

    private void resync(long now) {
        redisService.setwe(LeakyBucketKey.leakyBucket, this.name, now, LeakyBucketKey.leakyBucket.expireSeconds());
    }
}
