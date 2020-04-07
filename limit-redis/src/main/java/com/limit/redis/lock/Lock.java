package com.limit.redis.lock;

public interface Lock {

    // 上锁
    boolean lock(int key) throws Exception;

    // 释放锁
    boolean unlock(int key);
}
