package com.limit.common.concurrent.leakylimiter;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LeakyLimiterFactory {

    private static final Map<String, LeakyLimiter> LEAKYLIMITERS = new ConcurrentHashMap<>();

    public LeakyLimiter getLeakyLimiter(LeakyLimiterConfig config) {
        LeakyLimiter leakyLimiter = LEAKYLIMITERS.get(config.getName());
        if (leakyLimiter == null) {
            LEAKYLIMITERS.putIfAbsent(config.getName(), new LeakyLimiter(config));
            leakyLimiter = LEAKYLIMITERS.get(config.getName());
        }
        return leakyLimiter;
    }
}
