package com.limit.common.limiter.permitlimiter;

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
     * 每两次添加令牌之间的时间间隔（逐个添加令牌），单位为纳秒
     */
    private long intervalNanos;

    /**
     * 上次更新的时间
     */
    private long lastUpdateTime;

    public PermitBucket(long maxPermits, long storedPermits, long intervalNanos, long lastUpdateTime) {
        this.maxPermits = maxPermits;
        this.storedPermits = storedPermits;
        this.intervalNanos = intervalNanos;
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * 更新当前持有的令牌数
     * 若当前时间晚于 lastUpdateTime，则计算该段时间内可以生成多少令牌，将生成的令牌加入令牌桶中并更新数据
     *
     * @param now 当前时间
     */
    public void reSync(long now, long storedPermitsToSpend) {
        if (now > lastUpdateTime) {
            long newStoredPermits = Math.min(maxPermits, storedPermits + (now - lastUpdateTime) / intervalNanos - storedPermitsToSpend);
            // now 距离 lastUpdateTime 很短时，防止 lastUpdateTime 变了而 storedPermits 没变
            if (newStoredPermits != storedPermits) {
                storedPermits = newStoredPermits;
                lastUpdateTime = now;
            }
        }
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

    public long getIntervalNanos() {
        return intervalNanos;
    }

    public void setIntervalNanos(long intervalNanos) {
        this.intervalNanos = intervalNanos;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
