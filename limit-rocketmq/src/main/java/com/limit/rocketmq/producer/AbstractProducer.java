package com.limit.rocketmq.producer;

import com.limit.rocketmq.config.ProducerConfig;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProducer implements Producer {

    private static final Logger log = LoggerFactory.getLogger(AbstractProducer.class);

    protected final DefaultMQProducer producer;
    private final String name;

    public AbstractProducer(String name, ProducerConfig config) {
        this.name = name;
        DefaultMQProducer producer = new DefaultMQProducer(config.getGroupName());
        producer.setNamesrvAddr(config.getNamesrvAddr());
        producer.setRetryTimesWhenSendFailed(config.getRetryTimesWhenSendFailed());
        this.producer = producer;
        try {
            this.producer.start();
        } catch (Exception e) {
            log.info(e.toString());
        }
    }

    @Override
    public void destroy() {
        this.producer.shutdown();
        ProducerFactory.destroy(this);
    }
}
