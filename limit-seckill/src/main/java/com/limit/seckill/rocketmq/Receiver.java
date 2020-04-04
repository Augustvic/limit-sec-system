package com.limit.seckill.rocketmq;

import com.alibaba.fastjson.JSON;
import com.limit.redis.service.RedisService;
import com.limit.seckill.service.SeckillService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(topic = Config.SECKILL_TOPIC, consumerGroup = "my-consumer_test-topic-1")
public class Receiver implements RocketMQListener<SeckillMessage> {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedisService redisService;

    public void onMessage(SeckillMessage message) {
        String msg = RedisService.beanToString(message);
        System.out.println("--------------------");
        System.out.println("receive: " + msg);
        System.out.println("--------------------");
        try {
            seckillService.afterReceiveMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
