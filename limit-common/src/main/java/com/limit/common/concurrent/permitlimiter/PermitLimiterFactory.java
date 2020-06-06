package com.limit.common.concurrent.permitlimiter;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PermitLimiterFactory {

    private static final Map<String, PermitLimiter> RATELIMITERS = new ConcurrentHashMap<>();

    public PermitLimiter getPermitLimiter(PermitLimiterConfig config) {
        PermitLimiter permitLimiter = RATELIMITERS.get(config.getName());
        if (permitLimiter == null) {
            RATELIMITERS.putIfAbsent(config.getName(), new PermitLimiter(config));
            permitLimiter = RATELIMITERS.get(config.getName());
        }
        return permitLimiter;
    }
}
