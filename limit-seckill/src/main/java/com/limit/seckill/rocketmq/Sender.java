package com.limit.seckill.rocketmq;

import com.limit.redis.service.RedisService;
import com.limit.seckill.exchange.DefaultFuture;
import com.limit.seckill.exchange.message.Request;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class Sender {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendSeckillRequest(Request request) throws Exception{
        rocketMQTemplate.send(Config.SECKILL_TOPIC, MessageBuilder.withPayload(request).build());
    }
}
