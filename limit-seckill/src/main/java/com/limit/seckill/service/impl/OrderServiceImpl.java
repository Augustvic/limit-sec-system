package com.limit.seckill.service.impl;

import com.limit.redis.key.seckill.OrderKey;
import com.limit.redis.service.RedisService;
import com.limit.seckill.dao.OrderDao;
import com.limit.seckill.entity.SeckillOrder;
import com.limit.seckill.entity.OrderInfo;
import com.limit.seckill.service.OrderService;
import com.limit.seckill.vo.GoodsVo;
import com.limit.user.entity.SeckillUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    public SeckillOrder getSeckillOrderByUserIdGoodsId(long userId, long goodsId) {
//        从数据库中获取秒杀订单信息
//        return orderDao.getSeckillOrderByUserIdGoodsId(userId, goodsId);
        // 从缓存中获取秒杀订单信息
        return redisService.hget(OrderKey.getSeckillOrderByUidGid, "" + userId, "g" + goodsId, SeckillOrder.class);
    }

    @Transactional
    public OrderInfo createOrder(SeckillUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderDao.insert(orderInfo);
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(goods.getId());
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setUserId(user.getId());
        orderDao.insertSeckillOrder(seckillOrder);

        // 将秒杀订单信息写入缓存
        redisService.hset(OrderKey.getSeckillOrderByUidGid, "" + user.getId(), "g" + goods.getId(), seckillOrder);

        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }
}
