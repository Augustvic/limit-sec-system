package com.miaosha.util.concurrent;

import com.google.common.math.LongMath;
import com.miaosha.redis.PermitBucketKey;
import com.miaosha.redis.RedisService;
import com.miaosha.util.BaseUtil;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * 令牌桶限流器，以令牌桶为基础
 */
public class RateLimiter {

    @Autowired
    RedisService redisService;

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

    /**
     * 构造函数
     * @param key 令牌桶 key 后缀
     * @param permitsPerSecond 每秒存入的令牌数
     * @param maxPermits 最大存储令牌数
     */
    public RateLimiter(String key, Long permitsPerSecond, Long maxPermits, RLock lock){
        this.key = key;
        this.permitsPerSecond = permitsPerSecond;
        this.maxPermits = maxPermits;
        this.lock = lock;
    }

    /**
     * 尝试获取锁
     * @return 获取成功返回 true
     */
    private boolean lock(){
        try {
            // 等待 100 秒，获得锁 10 秒后自动解锁
            return lock.tryLock(100, 10, TimeUnit.SECONDS);
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
     * @return 返回令牌桶
     */
    private PermitBucket putDefaultBucket() {
        while (!redisService.exists(PermitBucketKey.permitBucket, this.key)) {
            if (lock()) {
                try {
                    if (!redisService.exists(PermitBucketKey.permitBucket, this.key)) {
                        // 初始化 PermitBucket
                        permitsPerSecond = (permitsPerSecond == 0L) ? 1000L : permitsPerSecond;
                        long intervalMillis = TimeUnit.SECONDS.toMillis(1) / permitsPerSecond;
                        long nextFreeTicketMillis = System.currentTimeMillis();
                        PermitBucket permitBucket = new PermitBucket(maxPermits, permitsPerSecond, intervalMillis, nextFreeTicketMillis);
                        // 存入缓存
                        redisService.setwe(PermitBucketKey.permitBucket, this.key, permitBucket, BaseUtil.safeLongToInt(permitBucket.expires()));
                        return permitBucket;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    unlock();
                }
            }
        }
        return redisService.get(PermitBucketKey.permitBucket, this.key, PermitBucket.class);
    }

    /**
     * 获取令牌桶
     * @return 缓存中的令牌桶或者默认的令牌桶
     */
    public PermitBucket getBucket() {
        PermitBucket permitBucket = redisService.get(PermitBucketKey.permitBucket, this.key, PermitBucket.class);
        if (permitBucket == null) {
            return putDefaultBucket();
        }
        return permitBucket;
    }

    /**
     * 更新令牌桶
     * @param permitBucket 新的令牌桶
     */
    public void setBucket(PermitBucket permitBucket) {
        redisService.setwe(PermitBucketKey.permitBucket, this.key, permitBucket, BaseUtil.safeLongToInt(permitBucket.expires()));
    }

    /**
     * 等待直到获取指定数量的令牌
     * @param tokens 请求令牌数量
     * @return 如果需要等待，线程休眠，再次就绪后返回等待的时间，否则返回 0
     */
    public boolean acquire(Long tokens){
        try {
            Long milliToWait = reserve(tokens);
            Thread.sleep(milliToWait);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取一个令牌
     *
     * @return 等待时间
     */
    public boolean acquire(){
        return acquire(1L);
    }

    /**
     * 获取令牌，有时间限制
     *
     * @param tokens 要获取的令牌数
     * @param timeout 获取令牌等待的时间，负数被视为0
     * @param unit 时间格式
     * @return 成功返回 true
     * @throws InterruptedException
     */
    public Boolean tryAcquire(Long tokens, Long timeout, TimeUnit unit) throws InterruptedException{
        long timeoutMicros = Math.max(unit.toMillis(timeout), 0);
        checkTokens(tokens);
        try {
            if (lock()) {
                if (!canAcquire(tokens, timeoutMicros)) {
                    return false;
                } else {
                    Long milliToWait = reserveAndGetWaitLength(tokens);
                    Thread.sleep(milliToWait);
                    return true;
                }
            }
        } finally {
            unlock();
        }
        return false;
    }

    /**
     * 获取一个令牌
     * @param timeout 时间限制
     * @param unit 时间格式
     * @return 成功返回 true
     * @throws InterruptedException
     */
    private Boolean tryAcquire(Long timeout , TimeUnit unit) throws InterruptedException{
        return tryAcquire(1L,timeout, unit);
    }

    /**
     * 在等待的时间内是否可以获取到令牌
     * @param tokens 请求令牌个数
     * @param timeoutMillis 时间限制
     * @return 可以获取返回 true
     */
    private Boolean canAcquire(Long tokens, Long timeoutMillis){
        return queryEarliestAvailable(tokens) - timeoutMillis <= 0;
    }

    /**
     * 获取 tokens 个令牌最早需要等待多久
     * @param tokens 令牌个数
     * @return 最短等待时间
     */
    private Long queryEarliestAvailable(Long tokens){
        long n = System.currentTimeMillis();
        PermitBucket bucket = getBucket();
        bucket.reSync(n);
        // 可以消耗的令牌数
        long storedPermitsToSpend = Math.min(tokens, bucket.getStoredPermits());
        // 需要等待的令牌数
        long freshPermits = tokens - storedPermitsToSpend;
        // 需要等待的时间
        long waitMillis = freshPermits * bucket.getIntervalMillis();
        return LongMath.checkedAdd(bucket.getNextFreeTicketMillis() - n, waitMillis);
    }

    /**
     * 获取令牌 n 个需要等待的时间
     * @param tokens 获取 tokens 个令牌
     * @return 返回需要等待的时间
     */
    private Long reserve(Long tokens) {
        checkTokens(tokens);
        while (true) {
            if (lock()) {
                try {
                    return reserveAndGetWaitLength(tokens);
                } finally {
                    unlock();
                }
            }
        }
    }

    /**
     * 校验 token 值
     * @param tokens token 值
     */
    private void checkTokens(Long tokens) {
        if (tokens < 0){
            throw new IllegalArgumentException("Requested tokens " + tokens + " must be positive");
        }
    }


    /**
     * 预定 tokens 个令牌并返回所需要等待的时间
     * @param tokens 请求 token 个令牌
     * @return 需要等待的时间
     */
    private Long reserveAndGetWaitLength(Long tokens){
        long timeMillis = System.currentTimeMillis();
        PermitBucket bucket = getBucket();
        bucket.reSync(timeMillis);
        // 可以消耗的令牌数
        long storedPermitsToSpend = Math.min(tokens, bucket.getStoredPermits());
        // 需要等待的令牌数
        long freshPermits = tokens - storedPermitsToSpend;
        // 需要等待的时间
        long waitMillis = freshPermits * bucket.getIntervalMillis();
        bucket.setNextFreeTicketMillis(LongMath.checkedAdd(bucket.getNextFreeTicketMillis(), waitMillis));
        bucket.setStoredPermits(bucket.getStoredPermits() - storedPermitsToSpend );
        setBucket(bucket);
        return bucket.getNextFreeTicketMillis() - timeMillis;
    }
}
