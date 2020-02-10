package com.miaosha.util.concurrent;

import com.google.common.math.LongMath;
import com.miaosha.redis.LockKey;
import com.miaosha.redis.PermitBucketKey;
import com.miaosha.redis.RedisService;
import com.miaosha.util.BaseUtil;
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
     * 构造函数
     * @param key 令牌桶 key 后缀
     * @param permitsPerSecond 每秒存入的令牌数
     * @param maxPermits 最大存储令牌数
     */
    public RateLimiter(String key, Long permitsPerSecond, Long maxPermits){
        this.key = key;
        this.permitsPerSecond = permitsPerSecond;
        this.maxPermits = maxPermits;
    }

    /**
     * 获取锁
     * @return 获取成功返回 true
     */
    private boolean lock() {
        return redisService.lock(LockKey.lock, PermitBucketKey.permitBucket.getPrefix() + this.key, "1");
    }

    /**
     * 释放锁
     * @return 释放成功返回 true
     */
    private boolean unlock() {
        return redisService.unlock(LockKey.lock, PermitBucketKey.permitBucket.getPrefix() + this.key, "1");
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
                        PermitBucket permitBucket = new PermitBucket(permitsPerSecond, maxPermits);
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
     * @return 等待时间
     * @throws InterruptedException
     */
    public Long acquire(Long tokens) throws InterruptedException {
        Long milliToWait = reserve(tokens);
        Thread.sleep(milliToWait);
        return milliToWait;
    }

    /**
     * 获取令牌 n 个需要等待的时间
     * @param tokens 获取 tokens 个令牌
     * @return 返回需要等待的时间
     */
    private Long reserve(Long tokens) {
        checkTokens(tokens);
        try {
            lock();
            return reserveAndGetWaitLength(tokens);
        } finally {
            unlock();
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
        bucket.setNextFreeTicketMillis(bucket.getNextFreeTicketMillis() + waitMillis);
        bucket.setStoredPermits(bucket.getStoredPermits() - storedPermitsToSpend );
        setBucket(bucket);
        return bucket.getNextFreeTicketMillis() - timeMillis;
    }
}
