package com.miaosha.util.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * 令牌桶
 */
public class PermitBucket {

    /**
     * 最大存储令牌数
     */
    private Long maxPermits;

    /**
     * 当前存储令牌数
     */
    private Long storedPermits;

    /**
     * 每两次添加令牌之间的时间间隔（逐个添加令牌）
     */
    private Long intervalMillis;

    /**
     * 下次可以请求获取令牌的起始时间，默认当前系统时间
     */
    private Long nextFreeTicketMillis;

    /**
     * 构造函数
     * @param permitsPerSecond 每秒放入的令牌数
     * @param maxPermits 最大存储令牌数
     */
    public PermitBucket(Long permitsPerSecond, Long maxPermits ) {
        if (permitsPerSecond == null) {
            permitsPerSecond = 100L;
        }
        if (maxPermits == null) {
            maxPermits = 1000L;
        }
        permitsPerSecond = (permitsPerSecond == 0L) ? 1000L : permitsPerSecond;
        this.maxPermits = maxPermits;
        this.storedPermits = permitsPerSecond;
        this.intervalMillis = TimeUnit.SECONDS.toMillis(1) / permitsPerSecond;
        this.nextFreeTicketMillis = System.currentTimeMillis();
    }

    /**
     * redis的过期时长。最低有效时长为 1 分钟。
     * @return 有效时长
     */
    public long expires() {
        long now = System.currentTimeMillis();
        return 2 * TimeUnit.MINUTES.toSeconds(1)
                + TimeUnit.MILLISECONDS.toSeconds(Math.max(nextFreeTicketMillis, now) - now);
    }

    /**
     * 异步更新当前持有的令牌数
     * 若当前时间晚于 nextFreeTicketMicros，则计算该段时间内可以生成多少令牌，将生成的令牌加入令牌桶中并更新数据
     * 此处没有更新 redis
     * @param now 当前时间
     * @return 是否更新成功
     */
    public boolean reSync(long now){
        if (now > nextFreeTicketMillis) {
            storedPermits = Math.min(maxPermits, storedPermits + (now - nextFreeTicketMillis) / intervalMillis);
            nextFreeTicketMillis = now;
            return true;
        }
        return false;
    }

    public void setStoredPermits(Long storedPermits) {
        this.storedPermits = storedPermits;
    }

    public Long getStoredPermits() {
        return storedPermits;
    }

    public Long getIntervalMillis() {
        return intervalMillis;
    }

    public void setNextFreeTicketMillis(Long nextFreeTicketMillis) {
        this.nextFreeTicketMillis = nextFreeTicketMillis;
    }

    public Long getNextFreeTicketMillis() {
        return nextFreeTicketMillis;
    }
}
