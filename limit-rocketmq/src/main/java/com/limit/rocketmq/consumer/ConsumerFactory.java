package com.limit.rocketmq.consumer;

import com.limit.rocketmq.config.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConsumerFactory {

    @Autowired
    protected ConsumerConfig consumerConfig;

    protected static final Logger log = LoggerFactory.getLogger(ConsumerFactory.class);

    public static final Map<String, Consumer> NAME_CONSUMER = new ConcurrentHashMap<>();
    public static final Map<Consumer, String> CONSUMER_NAME = new ConcurrentHashMap<>();

    public Consumer getConsumer(String name, String path) {
        Consumer consumer = NAME_CONSUMER.get(name);
        if (consumer == null) {
            try {
                Class clazz = Class.forName(path);
                Constructor clazzConstructor = clazz.getDeclaredConstructor(new Class[]{String.class, ConsumerConfig.class});
                consumer = (Consumer)clazzConstructor.newInstance(name, consumerConfig);
            } catch (Exception e) {
                log.info(e.toString());
            }
            NAME_CONSUMER.put(name, consumer);
            CONSUMER_NAME.put(consumer, name);
        }
        consumer = NAME_CONSUMER.get(name);
        return consumer;
    }

    public void destroy(Consumer consumer) {
        String name = CONSUMER_NAME.remove(consumer);
        NAME_CONSUMER.remove(name);
    }
}
