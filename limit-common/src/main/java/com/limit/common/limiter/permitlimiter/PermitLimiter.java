package com.limit.common.limiter.permitlimiter;

import com.limit.common.limiter.Limiter;
import com.limit.redis.key.common.PermitBucketKey;
import com.limit.redis.service.RedisService;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 令牌桶限流器，以令牌桶为基础
 */
public class PermitLimiter implements Limiter {

    private static final Logger log = LoggerFactory.getLogger(PermitLimiter.class);

    private final RedisService redisService;

    // 唯一标识
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

    /**
     * 构造函数
     */
    public PermitLimiter(PermitLimiterConfig config){
        this.name = config.getName();
        this.permitsPerSecond = (config.getPermitsPerSecond() == 0L) ? 1000L : config.getPermitsPerSecond();
        this.maxPermits = config.getMaxPermits();
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
            return lock.tryLock(100, 100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 释放锁
     */
    private void unlock() {
        lock.unlock();
    }

    /**
     * 生成并存储默认令牌桶
     * 必须在 lock 内调用
     * @return 返回令牌桶
     */
    private PermitBucket putDefaultBucket() {
        if (!redisService.exists(PermitBucketKey.permitBucket, this.name)) {
            long intervalNanos = TimeUnit.SECONDS.toNanos(1) / permitsPerSecond;
            long lastUpdateTime = System.nanoTime();
            PermitBucket permitBucket = new PermitBucket(name, maxPermits, permitsPerSecond, intervalNanos, lastUpdateTime);
            // 存入缓存，设置有效时间
            redisService.setwe(PermitBucketKey.permitBucket, this.name, permitBucket, PermitBucketKey.permitBucket.expireSeconds());
        }
        return redisService.get(PermitBucketKey.permitBucket, this.name, PermitBucket.class);
    }

    /**
     * 获取令牌桶
     * @return 缓存中的令牌桶或者默认的令牌桶
     */
    public PermitBucket getBucket() {
        // 从缓存中获取桶
        PermitBucket permitBucket = redisService.get(PermitBucketKey.permitBucket, this.name, PermitBucket.class);
        // 如果缓存中没有，进入 putDefaultBucket 中初始化
        if (permitBucket == null) {
            return putDefaultBucket();
        }
        return permitBucket;
    }

    /**
     * 更新令牌桶
     * @param permitBucket 新的令牌桶
     */
    private void setBucket(PermitBucket permitBucket) {
        redisService.setwe(PermitBucketKey.permitBucket, this.name, permitBucket, PermitBucketKey.permitBucket.expireSeconds());
    }
    
    /**
     * 尝试获取 permits 个令牌
     *
     * @return 获取成功返回 true，失败返回 false
     */
    public boolean acquire(long permits) {
        checkPermits(permits);
        while (true) {
            if (lock()) {
                long wait;
                try {
                    wait = canAcquire(permits);
                    if (wait <= 0L) {
                        return doAcquire(permits);
                    }
                    else {
                        return false;
                    }
                } finally {
                    unlock();
                }
            }
        }
    }

    /**
     * 获取一个令牌
     *
     * @return 成功返回 true
     */
    @Override
    public boolean acquire() {
        return acquire(1);
    }

    /**
     * 获取成功或超时才返回
     * @param permits 获取的令牌数
     * @param timeout 超时时间，单位为秒
     */
    public boolean acquireTillSuccess(long permits, long timeout) {
        checkPermits(permits);
        long start = System.nanoTime();
        long timeoutNanos = TimeUnit.SECONDS.toNanos(timeout);
        while (true) {
            long wait = 0L;
            if (lock()) {
                try {
                    wait = canAcquire(permits);
                    if (wait <= 0L && doAcquire(permits)) {
                        return true;
                    }
                } finally {
                    unlock();
                }
            }
            try {
                Thread.sleep(TimeUnit.NANOSECONDS.toMillis(wait));
            } catch (Exception e) {
                log.info(e.toString());
            }
            if (System.nanoTime() - start > timeoutNanos)
                return false;
        }
    }

    /**
     * 添加指定数量令牌
     * @param permits 要添加的令牌数
     */
    public void addPermits(long permits) {
        checkPermits(permits);
        while (true) {
            if (lock()) {
                try {
                    PermitBucket bucket = getBucket();
                    long now = System.nanoTime();
                    bucket.reSync(now, 0L);
                    long newPermits = calculateAddPermits(bucket, permits);
                    bucket.setStoredPermits(newPermits);
                    setBucket(bucket);
                    return;
                } finally {
                    unlock();
                }
            }
        }
    }

    /**
     * 计算添加之后桶里的令牌数
     * @param bucket 桶
     * @param addPermits 添加的令牌数
     * @return
     */
    private long calculateAddPermits(PermitBucket bucket, long addPermits) {
        long newPermits = bucket.getStoredPermits() + addPermits;
        if (newPermits > bucket.getMaxPermits()) {
            newPermits = bucket.getMaxPermits();
        }
        return newPermits;
    }

    /**
     * 当前是否可以获取到令牌，如果获取不到，至少需要等多久
     * @param permits 请求的令牌数
     * @return 等待时间，单位是纳秒。为 0 表示可以马上获取
     */
    private long canAcquire(long permits){
        PermitBucket bucket = getBucket();
        long now = System.nanoTime();
        bucket.reSync(now, 0L);
        setBucket(bucket);
        if (permits <= bucket.getStoredPermits()) {
            return 0L;
        }
        else {
            return (permits - bucket.getStoredPermits()) * bucket.getIntervalNanos();
        }
    }

    /**
     * 确认可以获取，就获取 permits 个令牌，更新缓存
     * @param permits 请求 token 个令牌
     * @return 需要等待的时间
     */
    private boolean doAcquire(long permits) {
        PermitBucket bucket = getBucket();
        if (permits > bucket.getStoredPermits())
            return false;
        // 当前时间
        long now = System.nanoTime();
        if (now > bucket.getLastUpdateTime()) {
            // 可以消耗的令牌数/需要消耗的令牌数
            long storedPermitsToSpend = Math.min(permits, bucket.getStoredPermits());
            // 更新一下
            bucket.reSync(now, storedPermitsToSpend);
            // 缓存中更新桶的状态
            setBucket(bucket);
            return true;
        }
        return false;
    }

    /**
     * 校验 token 值
     * @param permits token 值
     */
    private void checkPermits(long permits) {
        if (permits < 0) {
            throw new IllegalArgumentException("Request/Put permits " + permits + " must be positive");
        }
    }
}
