package com.limit.redis.lock;

import com.limit.redis.service.RedissonService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LockFactory{

    private static final Map<String, Lock> LOCKS = new ConcurrentHashMap<>();
    private static final Map<Lock, String> LOCK_NAME = new ConcurrentHashMap<>();

    public DLock getDLock(String name, int nLocks, RedissonService redissonService) {
        Lock lock = LOCKS.get(name);
        if (lock == null) {
            lock = new DLock(name, nLocks, redissonService);
            LOCKS.putIfAbsent(name, lock);
            LOCK_NAME.putIfAbsent(lock, name);
            lock = LOCKS.get(name);
        }
        return (DLock) lock;
    }

    public void destroy(Lock lock) {
        String name = LOCK_NAME.remove(lock);
        LOCKS.remove(name);
    }
}
