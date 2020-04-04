package com.limit.redis;

public interface KeyPrefix {

    public int expireSeconds();

    public String getPrefix();
}
