package com.limit.common.concurrent;

import com.limit.redis.service.RedissonService;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Autowired
    RedissonService redissonService;

    @Bean
    RateLimiter rDBRateLimiter() {
        RLock readLock = redissonService.getRLock("readlock");
        return new RateLimiter("readLimiter", 100L, 1000L, readLock);
    }

    @Bean
    RateLimiter wDBRateLimiter() {
        RLock writeLock = redissonService.getRLock("writelock");
        return new RateLimiter("writeLimiter", 10L, 1000L, writeLock);
    }

}
