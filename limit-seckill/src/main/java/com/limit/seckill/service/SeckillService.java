package com.limit.seckill.service;

import com.limit.common.concurrent.RateLimiter;
import com.limit.redis.seckill.SeckillKey;
import com.limit.redis.service.RedisService;
import com.limit.redis.service.RedissonService;
import com.limit.seckill.dao.SeckillGoodsDao;
import com.limit.seckill.entity.SeckillGoods;
import com.limit.seckill.entity.SeckillOrder;
import com.limit.seckill.entity.OrderInfo;
import com.limit.common.utils.BaseUtil;
import com.limit.common.utils.MD5Util;
import com.limit.common.utils.UUIDUtil;
import com.limit.seckill.rocketmq.SeckillMessage;
import com.limit.seckill.rocketmq.Sender;
import com.limit.seckill.vo.GoodsVo;
import com.limit.user.entity.SeckillUser;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillService {

    @Autowired
    SeckillGoodsDao seckillGoodsDao;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Autowired
    RedissonService redissonService;

    @Autowired
    Sender sender;

    @Autowired
    @Qualifier(value = "rDBRateLimiter")
    RateLimiter readDBRateLimiter;

    @Autowired
    @Qualifier(value = "wDBRateLimiter")
    RateLimiter writeDBRateLimiter;

    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
        // 减库存 下订单 写入秒杀订单
        boolean success = reduceStock(goods);
        if (success) {
            // order_info, seckill_order
            return orderService.createOrder(user, goods);
        }
        else {
            setGoodsOver(goods.getId());
            return null;
        }
    }

    public long getSeckillResult(Long userId, long goodsId) {
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(userId, goodsId);
        // 秒杀成功
        if (order != null) {
            return order.getOrderId();
        }
        else {
            boolean isOver = getGoodsOver(goodsId);
            if (isOver)
                return -1;
            else
                return 0;
        }
    }

    //redis中标记商品已经卖完
    private void setGoodsOver(Long goodsId) {
        redisService.set(SeckillKey.isGoodsOver, "" + goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(SeckillKey.isGoodsOver, "" + goodsId);
    }

    // 检查秒杀地址
    public boolean checkPath(SeckillUser user, long goodsId, String path) {
        if (user == null || path == null) {
            return false;
        }
        String pathOld = redisService.get(SeckillKey.getSeckillPath, "" + user.getId() + "_" + goodsId, String.class);
        return path.equals(pathOld);
    }

    // 生成秒杀地址 path 参数，并存入缓存中
    public String createSeckillPath(SeckillUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(SeckillKey.getSeckillPath, "" + user.getId() + "_" + goodsId, str);
        return str;
    }

    public BufferedImage createVerifyCode(SeckillUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(SeckillKey.getSeckillVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    private int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * +,-,*
     */
    public String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    public boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0)
            return false;
        Integer codeOld = redisService.get(SeckillKey.getSeckillVerifyCode, user.getId()+","+goodsId, Integer.class);
        if (codeOld == null || codeOld - verifyCode != 0) {
            return false;
        }
        redisService.delete(SeckillKey.getSeckillVerifyCode, user.getId()+","+goodsId);
        return true;
    }

    /**
     * 减库存
     * @param goods 需要减库存的商品
     * @return 减库存成功返回 true
     */
    public boolean reduceStock(GoodsVo goods) {
        SeckillGoods g = new SeckillGoods();
        g.setGoodsId(goods.getId());
        int ret = seckillGoodsDao.reduceStock(g);
        return ret > 0;
    }

    /**
     * 获取指定时间内即将参加秒杀的商品
     * @param startSeconds 初始时间偏移
     * @param endSeconds 结束时间偏移
     * @return 得到的秒杀商品列表
     */
    public List<SeckillGoods> listSeckillGoodsLatest(int startSeconds, int endSeconds) {
        String startDate = BaseUtil.timeAdd(startSeconds);
        String endDate = BaseUtil.timeAdd(endSeconds);
        return  seckillGoodsDao.listSeckillGoodsLatest(startDate, endDate);
    }

    /**
     * 根据 id 获取秒杀商品
     * @param goodsId id
     * @return 秒杀商品
     */
    public SeckillGoods getSeckillGoodById(Long goodsId) {
        return seckillGoodsDao.getSeckillGoodById(goodsId);
    }


    /**
     * kafka 接收到消息之后，进行后续检查库存，写订单等操作
     * @param message 接收到的消息
     */
    public void afterReceiveMessage(String message) throws Exception{

        System.out.println("---------------------");
        System.out.println("receive: " + Thread.currentThread().getName());
        System.out.println("---------------------");

        SeckillMessage mm = RedisService.stringToBean(message, SeckillMessage.class);
        SeckillUser user = mm.getUser();
        long goodsId = mm.getGoodsId();

        // 如果没有获取到读数据库令牌
        if (!readDBRateLimiter.acquireMillis()) {
            // 重新进入队列等待
            sender.sendSeckillMessage(mm);
            return;
        }
        // 读取数据库中商品库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0)
            return;
        //判断是否已经秒杀到了
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        // 已经秒杀到了，不能重复秒杀
        if (order != null) {
            return;
        }
        // 如果没有获取到写数据库令牌
        if (!writeDBRateLimiter.acquireMillis()) {
            // 重新进入队列等待
            sender.sendSeckillMessage(mm);
            return;
        }
        //减库存 下订单 写入秒杀订单
        // 获取互斥锁
        RLock rLock = redissonService.getRLock("decrStockLock_" + goodsId);
        // 最多等待 100 秒，上锁 10 秒后解锁
        if (rLock.tryLock(100, 10, TimeUnit.SECONDS)) {
            try {
                seckill(user, goods);
            } finally {
                rLock.unlock();
            }
        }
    }
}
