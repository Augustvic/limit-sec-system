package com.limit.redis.key.seckill;

import com.limit.redis.key.BasePrefix;

public class GoodsKey extends BasePrefix {

    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static GoodsKey getSeckillGoodsStock = new GoodsKey(0, "gs");
}
