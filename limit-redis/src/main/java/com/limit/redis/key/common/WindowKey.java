package com.limit.redis.key.common;

import com.limit.redis.key.BasePrefix;

public class WindowKey extends BasePrefix {

    public WindowKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static WindowKey window = new WindowKey(0, "win");
}
