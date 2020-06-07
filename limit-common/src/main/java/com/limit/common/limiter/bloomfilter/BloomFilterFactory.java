package com.limit.common.limiter.bloomfilter;

import com.limit.common.Factory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BloomFilterFactory implements Factory {

    private static final Map<String, BloomFilter> BLOOMFILTERS = new ConcurrentHashMap<>();
    private static final Map<BloomFilter, String> BLOOMFILTER_NAME = new ConcurrentHashMap<>();

    public BloomFilter getBloomFilter(BloomFilterConfig config) {
        BloomFilter bloomFilter = BLOOMFILTERS.get(config.getName());
        if (bloomFilter == null) {
            bloomFilter = new BloomFilter(config);
            String name = config.getName();
            BLOOMFILTERS.putIfAbsent(name, bloomFilter);
            BLOOMFILTER_NAME.putIfAbsent(bloomFilter, name);
            bloomFilter = BLOOMFILTERS.get(name);
        }
        return bloomFilter;
    }

    @Override
    public void destroy(Object obj) {
        if (obj instanceof BloomFilter) {
            String name = BLOOMFILTER_NAME.remove(obj);
            BLOOMFILTERS.remove(name);
        }
    }
}
