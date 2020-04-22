package com.limit.redis.lock;

import com.limit.redis.service.RedissonService;

public class LockFactory {

    public static DLock getDLock(String name, int nLocks, RedissonService redissonService) {
        return new DLock(name, nLocks, redissonService);
    }
}
