package com.miaosha.kafka;

import com.miaosha.entity.MiaoshaOrder;
import com.miaosha.entity.MiaoshaUser;
import com.miaosha.redis.RedisService;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaService;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.service.OrderService;
import com.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @KafkaListener(topics = com.miaosha.kafka.MQConfig.MIAOSHA_QUEUE)
    public void receive(String message) {
        System.out.println("receive message:" + message);
        try {
            miaoshaService.afterReceiveMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
