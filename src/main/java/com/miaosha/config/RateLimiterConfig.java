package com.miaosha.config;

import com.miaosha.redis.RedissonService;
import com.miaosha.util.concurrent.RateLimiter;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Autowired
    RedissonService redissonService;

    @Bean
    RateLimiter rateLimiter() {
        RLock rLock = redissonService.getRLock("rlock");
        return new RateLimiter("limiter", 1L, 1000L, rLock);
    }
}
