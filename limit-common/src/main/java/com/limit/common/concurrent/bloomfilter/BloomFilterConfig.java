package com.limit.common.concurrent.bloomfilter;

import redis.clients.jedis.JedisPool;

public class BloomFilterConfig {
    // 预计插入量
    private long expectedInsertions = 1000;
    // 可接受的错误率
    private double fpp = 0.01d;

    private JedisPool jedisPool;

    public BloomFilterConfig(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public BloomFilterConfig(long expectedInsertions, double fpp, JedisPool jedisPool) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        this.jedisPool = jedisPool;
    }

    public long getExpectedInsertions() {
        return expectedInsertions;
    }

    public void setExpectedInsertions(long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public double getFpp() {
        return fpp;
    }

    public void setFpp(double fpp) {
        this.fpp = fpp;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
}
