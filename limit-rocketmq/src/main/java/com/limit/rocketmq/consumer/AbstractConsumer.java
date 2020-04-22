package com.limit.rocketmq.consumer;

import com.limit.rocketmq.config.ConsumerConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConsumer implements Consumer {

    protected static final Logger log = LoggerFactory.getLogger(AbstractConsumer.class);

    protected final DefaultMQPushConsumer consumer;
    private final String name;

    public AbstractConsumer(String name, ConsumerConfig config) {
        this.name = name;
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(config.getGroupName());
        consumer.setNamesrvAddr(config.getNamesrvAddr());
        consumer.setConsumeThreadMin(config.getConsumeThreadMin());
        consumer.setConsumeThreadMax(config.getConsumeThreadMax());
        this.consumer = consumer;
    }

    @Override
    public void destroy() {
        this.consumer.shutdown();
        ConsumerFactory.destroy(this);
    }
}
