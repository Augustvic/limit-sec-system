package com.miaosha.redis;

public class GoodsKey extends BasePrefix{

    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0, "gs");
}
