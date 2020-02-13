package com.miaosha.util.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * 令牌桶
 */
public class PermitBucket {

    /**
     * 最大存储令牌数
     */
    private long maxPermits;

    /**
     * 当前存储令牌数
     */
    private long storedPermits;

    /**
     * 每两次添加令牌之间的时间间隔（逐个添加令牌）
     */
    private long intervalMicros;

    /**
     * 下次可以请求获取令牌的起始时间，默认当前系统时间
     */
    private long nextFreeTicketMillis;

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
            storedPermits = Math.min(maxPermits, storedPermits + TimeUnit.MILLISECONDS.toMicros(now - nextFreeTicketMillis) / intervalMicros);
            nextFreeTicketMillis = now;
            return true;
        }
        return false;
    }

    public PermitBucket(long maxPermits, long storedPermits, long intervalMicros, long nextFreeTicketMillis) {
        this.maxPermits = maxPermits;
        this.storedPermits = storedPermits;
        this.intervalMicros = intervalMicros;
        this.nextFreeTicketMillis = nextFreeTicketMillis;
    }

    public long getMaxPermits() {
        return maxPermits;
    }

    public void setMaxPermits(long maxPermits) {
        this.maxPermits = maxPermits;
    }

    public long getStoredPermits() {
        return storedPermits;
    }

    public void setStoredPermits(long storedPermits) {
        this.storedPermits = storedPermits;
    }

    public long getIntervalMicros() {
        return intervalMicros;
    }

    public void setIntervalMicros(long intervalMicros) {
        this.intervalMicros = intervalMicros;
    }

    public long getNextFreeTicketMillis() {
        return nextFreeTicketMillis;
    }

    public void setNextFreeTicketMillis(long nextFreeTicketMillis) {
        this.nextFreeTicketMillis = nextFreeTicketMillis;
    }
}
