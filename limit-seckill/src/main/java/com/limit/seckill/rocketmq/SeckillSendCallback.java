package com.limit.seckill.rocketmq;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeckillSendCallback implements SendCallback {

    private static final Logger log = LoggerFactory.getLogger(SeckillSendCallback.class);

    @Override
    public void onSuccess(SendResult sendResult) {
        log.info(sendResult.toString());
    }

    @Override
    public void onException(Throwable e) {
        log.info(e.toString());
    }
}
