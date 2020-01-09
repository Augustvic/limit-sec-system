package com.miaosha.kafka;

import com.miaosha.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MQSender {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMiaoshaMessage(com.miaosha.kafka.MiaoshaMessage mm) {
        String msg = RedisService.beanToString(mm);
        System.out.println("send message:" + msg);
        kafkaTemplate.send(MQConfig.MIAOSHA_QUEUE, msg);
    }
}
