package com.limit.common.concurrent;

/**
 * 限流器
 */
public interface Limiter {
    boolean acquire();
}
