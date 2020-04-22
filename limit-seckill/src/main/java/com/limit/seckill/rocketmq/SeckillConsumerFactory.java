package com.limit.seckill.rocketmq;

import com.limit.rocketmq.consumer.ConsumerFactory;
import com.limit.seckill.dispatcher.support.DoSeckillDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillConsumerFactory extends ConsumerFactory {

    @Autowired
    DoSeckillDispatcher doSeckillDispatcher;

    public SeckillConsumer getSeckillConsumer(String name) {
        return new SeckillConsumer(name, consumerConfig, doSeckillDispatcher);
    }
}