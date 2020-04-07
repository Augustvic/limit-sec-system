package com.limit.seckill.exchange.message;

import com.limit.user.entity.SeckillUser;

import java.util.concurrent.atomic.AtomicLong;

public class Request {

    private static final AtomicLong REQUEST_ID = new AtomicLong(0);

    private final long id;
    private SeckillUser user;
    private long goodsId;

    public Request() {
        id = newId();
    }

    public Request(SeckillUser user, long goodsId) {
        id = newId();
        this.user = user;
        this.goodsId = goodsId;
    }

    private long newId() {
        return REQUEST_ID.getAndIncrement();
    }

    public long getId() {
        return id;
    }

    public SeckillUser getUser() {
        return user;
    }

    public void setUser(SeckillUser user) {
        this.user = user;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }
}
