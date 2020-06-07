package com.limit.common.limiter.leakylimiter;

import com.limit.common.Factory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LeakyLimiterFactory implements Factory {

    private static final Map<String, LeakyLimiter> LEAKYLIMITERS = new ConcurrentHashMap<>();
    private static final Map<LeakyLimiter, String> LEAKYLIMITER_NAME = new ConcurrentHashMap<>();

    public LeakyLimiter getLeakyLimiter(LeakyLimiterConfig config) {
        LeakyLimiter leakyLimiter = LEAKYLIMITERS.get(config.getName());
        if (leakyLimiter == null) {
            leakyLimiter = new LeakyLimiter(config);
            String name = config.getName();
            LEAKYLIMITERS.putIfAbsent(name, leakyLimiter);
            LEAKYLIMITER_NAME.putIfAbsent(leakyLimiter, name);
            leakyLimiter = LEAKYLIMITERS.get(name);
        }
        return leakyLimiter;
    }

    @Override
    public void destroy(Object obj) {
        if (obj instanceof LeakyLimiter) {
            String name = LEAKYLIMITER_NAME.remove(obj);
            LEAKYLIMITERS.remove(name);
        }
    }
}
