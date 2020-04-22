package com.limit.redis.lock;

import com.limit.redis.service.RedissonService;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DLock implements Lock {

    private RedissonService redissonService;

    // 锁名字的前缀
    private final String name;

    // 锁粒度
    private final int nLocks;

    // 每把锁的标识
    private final String[] keys;

    // 缓存已经用过的锁
    private final Map<String, RLock> RLOCKS;

    public DLock(String name, int nLocks,  RedissonService redissonService) {
        this.name = name;
        this.nLocks = nLocks;
        this.keys = new String[nLocks];
        for (int i = 0; i < this.nLocks; i++) {
            this.keys[i] = this.name + i;
        }
        this.RLOCKS = new ConcurrentHashMap<>();
        this.redissonService = redissonService;
    }

    @Override
    public boolean lock(int key) throws Exception{
        boolean success = false;
        try {
            success = lock(key, 100, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw e;
        }
        return success;
    }

    public boolean lock(int key, long waitTime, long leaseTime, TimeUnit unit) throws Exception{
        RLock lock = getRLock(key);
        return lock.tryLock(waitTime, leaseTime, unit);
    }

    @Override
    public boolean unlock(int key) {
        RLock lock = getRLock(key);
        if (lock.isLocked())
            lock.unlock();
        return true;
    }

    private RLock getRLock(int key) {
        String keyName = keys[key];
        RLock lock = RLOCKS.get(keyName);
        if (lock == null) {
            lock = redissonService.getRLock(keyName);
            RLOCKS.put(keyName, lock);
        }
        return lock;
    }
}
