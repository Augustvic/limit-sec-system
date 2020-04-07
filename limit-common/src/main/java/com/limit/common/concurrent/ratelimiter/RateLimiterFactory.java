package com.limit.common.concurrent.ratelimiter;

public class RateLimiterFactory {

    public static RateLimiter getRateLimiter(RateLimiterConfig config) {
        return new RateLimiter(config);
    }
}
