package com.limit.redis.seckill;

import com.limit.redis.BasePrefix;

public class GoodsKey extends BasePrefix {

    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static GoodsKey getSeckillGoodsStock = new GoodsKey(0, "gs");
}
