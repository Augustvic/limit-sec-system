package com.limit.common.limiter.permitlimiter;

import com.limit.common.Factory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PermitLimiterFactory implements Factory {

    private static final Map<String, PermitLimiter> PERMITLIMITERS = new ConcurrentHashMap<>();
    private static final Map<PermitLimiter, String> PERMITLIMITER_NAME = new ConcurrentHashMap<>();

    public PermitLimiter getPermitLimiter(PermitLimiterConfig config) {
        PermitLimiter permitLimiter = PERMITLIMITERS.get(config.getName());
        if (permitLimiter == null) {
            permitLimiter = new PermitLimiter(config);
            String name = config.getName();
            PERMITLIMITERS.putIfAbsent(name, permitLimiter);
            PERMITLIMITER_NAME.putIfAbsent(permitLimiter, name);
            permitLimiter = PERMITLIMITERS.get(name);
        }
        return permitLimiter;
    }

    @Override
    public void destroy(Object obj) {
        if (obj instanceof PermitLimiter) {
            String name = PERMITLIMITER_NAME.remove(obj);
            PERMITLIMITERS.remove(name);
        }
    }
}
