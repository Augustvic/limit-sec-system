package com.miaosha.redis;

/**
 * Created by August on 2019/6/14 15:57
 **/

public interface KeyPrefix {

    public int expireSeconds();

    public String getPrefix();
}
