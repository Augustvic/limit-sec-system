package com.limit.rocketmq.producer;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

public interface Producer {

    /**
     * 同步发送消息
     * @param message 消息
     * @return 发送结果
     */
    SendResult sendSynchronously(String message) throws Exception;

    /**
     * 异步发送消息
     * @param message 消息
     * @param sendCallback 回调
     */
    void sendAsynchronously(String message, SendCallback sendCallback) throws Exception;

    /**
     * 不关心结果发送消息
     * @param message 消息
     */
    void sendOneWay(String message) throws Exception;

    /**
     * 销毁
     */
    void destroy();
}
