package com.miaosha.redis;

/**
 * Created by August on 2019/6/14 16:03
 **/
public class OrderKey extends BasePrefix {

    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey("moug");
}
