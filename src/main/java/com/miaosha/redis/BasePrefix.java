package com.miaosha.redis;

/**
 * Created by August on 2019/6/14 15:58
 **/

public abstract class BasePrefix implements com.miaosha.redis.KeyPrefix {

    private int expireSeconds;

    private String prefix;

    //默认0代表永不过期
    public BasePrefix(String prefix) {
        this.expireSeconds = 0;
        this.prefix = prefix;
    }

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    public int expireSeconds() {
        return expireSeconds;
    }

    public String getPrefix() {
        String className = getClass().getSimpleName();
        return className + ":" + prefix;
    }
}
