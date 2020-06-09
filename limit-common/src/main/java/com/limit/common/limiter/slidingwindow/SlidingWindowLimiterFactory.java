package com.limit.common.limiter.slidingwindow;

import com.limit.common.Factory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SlidingWindowLimiterFactory implements Factory {

    private static final Map<String, SlidingWindowLimiter> SLIDINGWINDOWLIMITERS = new ConcurrentHashMap<>();
    private static final Map<SlidingWindowLimiter, String> SLIDINGWINDOWLIMITER_NAME = new ConcurrentHashMap<>();

    public SlidingWindowLimiter getSlidingWindowLimiter(SlidingWindowLimiterConfig config) {
        SlidingWindowLimiter slidingWindowLimiter = SLIDINGWINDOWLIMITERS.get(config.getName());
        if (slidingWindowLimiter == null) {
            slidingWindowLimiter = new SlidingWindowLimiter(config);
            String name = config.getName();
            SLIDINGWINDOWLIMITERS.putIfAbsent(name, slidingWindowLimiter);
            SLIDINGWINDOWLIMITER_NAME.putIfAbsent(slidingWindowLimiter, name);
            slidingWindowLimiter = SLIDINGWINDOWLIMITERS.get(name);
        }
        return slidingWindowLimiter;
    }

    @Override
    public void destroy(Object obj) {
        if (obj instanceof SlidingWindowLimiter) {
            String name = SLIDINGWINDOWLIMITER_NAME.remove(obj);
            SLIDINGWINDOWLIMITERS.remove(name);
        }
    }
}
