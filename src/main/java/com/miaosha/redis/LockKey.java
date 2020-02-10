package com.miaosha.redis;

public class LockKey extends BasePrefix{

    public LockKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    // 分布式锁默认过期时间 5 秒
    public static LockKey lock = new LockKey(5, "lock");
}
