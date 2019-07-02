package com.miaosha.redis;

public class GoodsKey extends BasePrefix{

    public static final int GOODSLIST_EXPIRE = 120;   //商品列表页面缓存有效时间120s
    public static final int GOODSDETAIL_EXPIRE = 120;   //商品详情页面缓存有效时间120s

    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static GoodsKey getGoodsList = new GoodsKey(GOODSLIST_EXPIRE, "gl");

    public static GoodsKey getGoodsDetail = new GoodsKey(GOODSDETAIL_EXPIRE, "gd");

    public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0, "gs");
}
