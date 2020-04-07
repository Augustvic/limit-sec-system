package com.limit.redis.lock;

public class LockFactory {

    public static DLock getDLock(String name, int nLocks) {
        return new DLock(name, nLocks);
    }
}
