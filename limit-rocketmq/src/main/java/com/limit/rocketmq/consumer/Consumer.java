package com.limit.rocketmq.consumer;

import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;

public interface Consumer {

    /**
     * 开始监听
     */
    void start();
}
