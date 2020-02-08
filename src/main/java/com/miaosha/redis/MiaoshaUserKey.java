package com.miaosha.redis;

public class MiaoshaUserKey extends BasePrefix {

    public static final int TOKEN_EXPIRE = 60 * 5;   //用户 cookie 有效时间 5 分钟

    private MiaoshaUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE, "tk");

    public static MiaoshaUserKey getById = new MiaoshaUserKey(TOKEN_EXPIRE, "id");
}
