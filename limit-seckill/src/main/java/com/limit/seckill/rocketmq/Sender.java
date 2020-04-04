package com.limit.seckill.rocketmq;

import com.limit.redis.service.RedisService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class Sender {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendSeckillMessage(com.limit.seckill.rocketmq.SeckillMessage mm) throws Exception{
        rocketMQTemplate.send(Config.SECKILL_TOPIC, MessageBuilder.withPayload(mm).build());
        String msg = RedisService.beanToString(mm);
        System.out.println("--------------------");
        System.out.println("send: " + msg);
        System.out.println("--------------------");
    }
}
