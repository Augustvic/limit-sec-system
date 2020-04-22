package com.limit.seckill.rocketmq;

import com.limit.common.utils.BaseUtil;
import com.limit.rocketmq.config.ConsumerConfig;
import com.limit.rocketmq.consumer.AbstractConsumer;
import com.limit.seckill.dispatcher.support.DoSeckillDispatcher;
import com.limit.seckill.exchange.message.Request;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SeckillConsumer extends AbstractConsumer {

    public static final String path = "com.limit.seckill.rocketmq.SeckillConsumer";

    private final DoSeckillDispatcher doSeckillDispatcher;

    public SeckillConsumer(String name, ConsumerConfig config, DoSeckillDispatcher doSeckillDispatcher) {
        super(name, config);
        this.doSeckillDispatcher = doSeckillDispatcher;
    }

    // test
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public void start() {
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt msg : list) {
                    try {
                        byte[] body = msg.getBody();
                        Request request = Request.stringToRequest(BaseUtil.ByteArrayToString(body));
                        System.out.println("Received: " + count.incrementAndGet());
                        doSeckillDispatcher.received(request);
                    } catch (Exception e) {
                        log.info(e.toString());
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        try {
            consumer.subscribe(SeckillMQConfig.SECKILL_TOPIC, "*");
            consumer.start();
        } catch (Exception e) {
            log.info(e.toString());
        }
    }
}
