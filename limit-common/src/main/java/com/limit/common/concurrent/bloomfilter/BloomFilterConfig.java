package com.limit.common.concurrent.bloomfilter;

public class BloomFilterConfig {
    // 预计插入量
    private long expectedInsertions = 1000;
    // 可接受的错误率
    private double fpp = 0.01d;

    public BloomFilterConfig() {
    }

    public BloomFilterConfig(long expectedInsertions, double fpp) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
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
}
