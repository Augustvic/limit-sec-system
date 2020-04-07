package com.limit.seckill.service;

import com.limit.seckill.entity.OrderInfo;
import com.limit.seckill.entity.SeckillGoods;
import com.limit.seckill.exchange.message.Request;
import com.limit.seckill.vo.GoodsVo;
import com.limit.user.entity.SeckillUser;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.List;

public interface SeckillService {

    OrderInfo seckill(SeckillUser user, GoodsVo goods);

    long getSeckillResult(Long userId, long goodsId);

    //redis中标记商品已经卖完
    void setGoodsOver(Long goodsId);

    boolean getGoodsOver(long goodsId);

    // 检查秒杀地址
    boolean checkPath(SeckillUser user, long goodsId, String path);

    // 生成秒杀地址 path 参数，并存入缓存中
    String createSeckillPath(SeckillUser user, long goodsId);

    BufferedImage createVerifyCode(SeckillUser user, long goodsId);

    int calc(String exp);

    /**
     * +,-,*
     */
    String generateVerifyCode(Random rdm);

    boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode);

    /**
     * 减库存
     * @param goods 需要减库存的商品
     * @return 减库存成功返回 true
     */
    boolean reduceStock(GoodsVo goods);

    /**
     * 获取指定时间内即将参加秒杀的商品
     * @param startSeconds 初始时间偏移
     * @param endSeconds 结束时间偏移
     * @return 得到的秒杀商品列表
     */
    List<SeckillGoods> listSeckillGoodsLatest(int startSeconds, int endSeconds);

    /**
     * 根据 id 获取秒杀商品
     * @param goodsId id
     * @return 秒杀商品
     */
    SeckillGoods getSeckillGoodById(Long goodsId);


    /**
     * 后续检查库存，写订单等操作
     * @param message 接收到的消息
     */
    void afterReceiveRequest(Request message) throws Exception;
}
