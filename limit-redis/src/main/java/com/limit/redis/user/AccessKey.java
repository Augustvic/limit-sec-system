package com.limit.redis.user;

import com.limit.redis.BasePrefix;

public class AccessKey extends BasePrefix {

    private AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static AccessKey withExpire(int expireSeconds) {
        return new AccessKey(expireSeconds, "access");
    }
}
