package com.limit.redis.seckill;

import com.limit.redis.BasePrefix;

public class OrderKey extends BasePrefix {

    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getSeckillOrderByUidGid = new OrderKey("order");
}
