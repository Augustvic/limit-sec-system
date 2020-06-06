package com.limit.common.concurrent.bloomfilter;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BloomFilterFactory {

    private static final Map<String, BloomFilter> BLOOMFILTERS = new ConcurrentHashMap<>();

    public BloomFilter getBloomFilter(BloomFilterConfig config) {
        BloomFilter bloomFilter = BLOOMFILTERS.get(config.getName());
        if (bloomFilter == null) {
            BLOOMFILTERS.putIfAbsent(config.getName(), new BloomFilter(config));
            bloomFilter = BLOOMFILTERS.get(config.getName());
        }
        return bloomFilter;
    }
}
