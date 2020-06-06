package com.limit.common.concurrent.bloomfilter;

import com.limit.common.Constants;
import redis.clients.jedis.JedisPool;

public class BloomFilterConfig {

    private final String name;

    // 预计插入量
    private final long expectedInsertions;
    // 可接受的错误率
    private final double fpp;

    private final JedisPool jedisPool;

    public BloomFilterConfig(String name, JedisPool jedisPool) {
        this(name, Constants.EXPECTED_INSERTIONS, Constants.FPP, jedisPool);
    }

    public BloomFilterConfig(String name, long expectedInsertions, double fpp, JedisPool jedisPool) {
        this.name = name;
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        this.jedisPool = jedisPool;
    }

    public long getExpectedInsertions() {
        return expectedInsertions;
    }

    public double getFpp() {
        return fpp;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public String getName() {
        return name;
    }
}
