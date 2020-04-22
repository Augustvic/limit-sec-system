package com.limit.redis.key;

public interface KeyPrefix {

    public int expireSeconds();

    public String getPrefix();
}
