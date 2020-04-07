package com.limit.seckill.service;

import com.limit.seckill.entity.OrderInfo;
import com.limit.seckill.entity.SeckillOrder;
import com.limit.seckill.vo.GoodsVo;
import com.limit.user.entity.SeckillUser;

public interface OrderService {

    SeckillOrder getSeckillOrderByUserIdGoodsId(long userId, long goodsId);

    OrderInfo createOrder(SeckillUser user, GoodsVo goods);

    OrderInfo getOrderById(long orderId);
}
