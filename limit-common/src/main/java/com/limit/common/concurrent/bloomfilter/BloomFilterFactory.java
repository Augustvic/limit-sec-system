package com.limit.common.concurrent.bloomfilter;

public class BloomFilterFactory {

    public static BloomFilter getBloomFilter(BloomFilterConfig config) {
        return new BloomFilter(config);
    }
}
