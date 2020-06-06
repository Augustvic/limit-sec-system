package com.limit.redis.key.common;

import com.limit.redis.key.BasePrefix;

public class LeakyBucketKey extends BasePrefix {

    public LeakyBucketKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static LeakyBucketKey leakyBucket = new LeakyBucketKey(0, "lb");
}
