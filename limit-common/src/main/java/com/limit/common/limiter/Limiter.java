package com.limit.common.limiter;

/**
 * 限流器
 */
public interface Limiter {
    boolean acquire();
}
