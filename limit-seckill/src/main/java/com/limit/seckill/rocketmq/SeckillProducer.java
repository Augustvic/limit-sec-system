package com.limit.seckill.rocketmq;

import com.limit.common.utils.BaseUtil;
import com.limit.rocketmq.config.ProducerConfig;
import com.limit.rocketmq.producer.AbstractProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.atomic.AtomicInteger;

public class SeckillProducer extends AbstractProducer {

    public static final String path = "com.limit.seckill.rocketmq.SeckillProducer";

    public SeckillProducer(String name, ProducerConfig config) {
        super(name, config);
    }

    @Override
    public SendResult sendSynchronously(String message) throws Exception{
        Message msg = new Message(SeckillMQConfig.SECKILL_TOPIC,
                BaseUtil.StringToByteArray(message));
        return  producer.send(msg);
    }

    // test
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public void sendOneWay(String message) throws Exception{
        Message msg = new Message(SeckillMQConfig.SECKILL_TOPIC,
                BaseUtil.StringToByteArray(message));
        producer.send(msg);
        System.out.println("Send: " + Thread.currentThread().getName() + ": " + count.incrementAndGet());
    }

    @Override
    public void sendAsynchronously(String message, SendCallback sendCallback) throws Exception{
        SendCallback callback = new SeckillSendCallback();
        Message msg = new Message(SeckillMQConfig.SECKILL_TOPIC, BaseUtil.StringToByteArray(message));
        producer.send(msg, callback);
    }

}
