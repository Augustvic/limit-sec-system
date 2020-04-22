package com.limit.rocketmq.producer;

import com.limit.rocketmq.config.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProducerFactory {

    private static final Logger log = LoggerFactory.getLogger(ProducerFactory.class);

    @Autowired
    ProducerConfig producerConfig;

    public static final Map<String, Producer> NAME_PRODUCER = new ConcurrentHashMap<>();
    public static final Map<Producer, String> PRODUCER_NAME = new ConcurrentHashMap<>();

    public Producer getProducer(String name, String path) {
        Producer producer = NAME_PRODUCER.get(name);
        if (producer == null) {
            try {
                Class clazz = Class.forName(path);
                Constructor clazzConstructor = clazz.getDeclaredConstructor(new Class[]{String.class, ProducerConfig.class});
                producer = (Producer)clazzConstructor.newInstance(name, producerConfig);
            } catch (Exception e) {
                log.info(e.toString());
            }
            NAME_PRODUCER.put(name, producer);
            PRODUCER_NAME.put(producer, name);
        }
        producer = NAME_PRODUCER.get(name);
        return producer;
    }

    public static void destroy(Producer producer) {
        String name = PRODUCER_NAME.remove(producer);
        NAME_PRODUCER.remove(name);
    }
}
