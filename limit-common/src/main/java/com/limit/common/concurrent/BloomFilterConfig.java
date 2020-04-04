package com.limit.common.concurrent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {

    @Bean
    BloomFilter bloomFilter() {
        // 预计插入量
        long expectedInsertions = 1000;
        // 可接受的错误率
        double fpp = 0.01d;
        return new BloomFilter(expectedInsertions, fpp);
    }
}
