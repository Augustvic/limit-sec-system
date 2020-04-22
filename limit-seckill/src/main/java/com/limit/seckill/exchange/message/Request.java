package com.limit.seckill.exchange.message;

import com.limit.common.utils.BaseUtil;
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

    public Request(long id) {
        this.id = id;
    }

    public Request(SeckillUser user, long goodsId) {
        id = newId();
        this.user = user;
        this.goodsId = goodsId;
    }

    public Request(long id, SeckillUser user, long goodsId) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Request&id=" + id + "&goodsId=" + goodsId + "&user=" + BaseUtil.beanToString(user);
    }

    public static Request stringToRequest(String str) {
        int pos = 0;
        while (pos < str.length() && str.charAt(pos) != '&') pos++;
        if (str.substring(0, pos).equals("Request")) {
            str = str.substring(pos + 1);
            Request request;
            if (str.substring(0, 3).equals("id=")) {
                // id
                str = str.substring(3);
                pos = 0;
                while (pos < str.length() && str.charAt(pos) != '&') pos++;
                request = new Request(Integer.valueOf(str.substring(0, pos)));
                // goodsId
                str = str.substring(pos + 9);
                pos = 0;
                while (pos < str.length() && str.charAt(pos) != '&') pos++;
                request.setGoodsId(Long.valueOf(str.substring(0, pos)));
                // user
                str = str.substring(pos + 6);
                request.setUser((SeckillUser)BaseUtil.stringToBean(str, SeckillUser.class));
                return request;
            }
        }
        return null;
    }
}
