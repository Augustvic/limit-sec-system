package com.limit.redis.key.user;

import com.limit.redis.BasePrefix;

public class SeckillUserKey extends BasePrefix {

    public static final int TOKEN_EXPIRE = 60 * 5;   //用户 cookie 有效时间 5 分钟

    private SeckillUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillUserKey token = new SeckillUserKey(TOKEN_EXPIRE, "tk");

    public static SeckillUserKey getById = new SeckillUserKey(TOKEN_EXPIRE, "id");
}
