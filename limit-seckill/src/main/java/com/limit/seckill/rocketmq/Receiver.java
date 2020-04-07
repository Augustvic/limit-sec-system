package com.limit.seckill.rocketmq;

import com.limit.redis.service.RedisService;
import com.limit.seckill.dispatcher.support.DoSeckillDispatcher;
import com.limit.seckill.exchange.DefaultFuture;
import com.limit.seckill.exchange.message.Request;
import com.limit.seckill.service.impl.SeckillServiceImpl;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RocketMQMessageListener(topic = Config.SECKILL_TOPIC, consumerGroup = "my-consumer-test-topic-1")
public class Receiver implements RocketMQListener<Request> {

    @Autowired
    SeckillServiceImpl seckillService;

    @Autowired
    RedisService redisService;

    @Autowired
    DoSeckillDispatcher doSeckillDispatcher;

    public void onMessage(Request request) {
        doSeckillDispatcher.received(request);
    }
}
