package com.miaosha.redis;

public class MiaoshaUserKey extends BasePrefix {

    public static final int TOKEN_EXPIRE = 3600 * 12;   //用户cookie有效时间12小时

    private MiaoshaUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE, "tk");

    public static MiaoshaUserKey getById = new MiaoshaUserKey(TOKEN_EXPIRE, "id");
}
