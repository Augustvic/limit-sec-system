package com.miaosha.service;

import com.miaosha.dao.MiaoshaGoodsDao;
import com.miaosha.entity.*;
import com.miaosha.kafka.MQSender;
import com.miaosha.kafka.MiaoshaMessage;
import com.miaosha.redis.MiaoshaKey;
import com.miaosha.redis.RedisService;
import com.miaosha.redis.RedissonService;
import com.miaosha.util.BaseUtil;
import com.miaosha.util.MD5Util;
import com.miaosha.util.UUIDUtil;
import com.miaosha.util.concurrent.RateLimiter;
import com.miaosha.vo.GoodsVo;
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
public class MiaoshaService {

    @Autowired
    MiaoshaGoodsDao miaoshaGoodsDao;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Autowired
    RedissonService redissonService;

    @Autowired
    MQSender sender;

    @Autowired
    @Qualifier(value = "rDBRateLimiter")
    RateLimiter readDBRateLimiter;

    @Autowired
    @Qualifier(value = "wDBRateLimiter")
    RateLimiter writeDBRateLimiter;

    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        // 减库存 下订单 写入秒杀订单
        boolean success = reduceStock(goods);
        if (success) {
            // order_info, miaosha_order
            return orderService.createOrder(user, goods);
        }
        else {
            setGoodsOver(goods.getId());
            return null;
        }
    }

    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
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
        redisService.set(MiaoshaKey.isGoodsOver, "" + goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver, "" + goodsId);
    }

    // 检查秒杀地址
    public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
        if (user == null || path == null) {
            return false;
        }
        String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);
        return path.equals(pathOld);
    }

    // 生成秒杀地址 path 参数，并存入缓存中
    public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, str);
        return str;
    }

    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
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
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
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

    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0)
            return false;
        Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
        if (codeOld == null || codeOld - verifyCode != 0) {
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
        return true;
    }

    /**
     * 减库存
     * @param goods 需要减库存的商品
     * @return 减库存成功返回 true
     */
    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        int ret = miaoshaGoodsDao.reduceStock(g);
        return ret > 0;
    }

    /**
     * 获取指定时间内即将参加秒杀的商品
     * @param startSeconds 初始时间偏移
     * @param endSeconds 结束时间偏移
     * @return 得到的秒杀商品列表
     */
    public List<MiaoshaGoods> listMiaoshaGoodsLatest(int startSeconds, int endSeconds) {
        String startDate = BaseUtil.timeAdd(startSeconds);
        String endDate = BaseUtil.timeAdd(endSeconds);
        return  miaoshaGoodsDao.listMiaoshaGoodsLatest(startDate, endDate);
    }

    /**
     * 根据 id 获取秒杀商品
     * @param goodsId id
     * @return 秒杀商品
     */
    public MiaoshaGoods getMiaoshaGoodById(Long goodsId) {
        return miaoshaGoodsDao.getMiaoshaGoodById(goodsId);
    }


    /**
     * kafka 接收到消息之后，进行后续检查库存，写订单等操作
     * @param message 接收到的消息
     */
    public void afterReceiveMessage(String message) throws Exception{
        MiaoshaMessage mm = RedisService.stringToBean(message, MiaoshaMessage.class);
        MiaoshaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();

        // 如果没有获取到读数据库令牌
        if (!readDBRateLimiter.acquireMillis()) {
            // 重新进入队列等待
            sender.sendMiaoshaMessage(mm);
            return;
        }
        // 读取数据库中商品库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0)
            return;
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        // 已经秒杀到了，不能重复秒杀
        if (order != null) {
            return;
        }
        // 如果没有获取到写数据库令牌
        if (!writeDBRateLimiter.acquireMillis()) {
            // 重新进入队列等待
            sender.sendMiaoshaMessage(mm);
            return;
        }
        //减库存 下订单 写入秒杀订单
        // 获取互斥锁
        RLock rLock = redissonService.getRLock("decrStockLock_" + goodsId);
        // 最多等待 100 秒，上锁 10 秒后解锁
        if (rLock.tryLock(100, 10, TimeUnit.SECONDS)) {
            try {
                miaosha(user, goods);
            } finally {
                rLock.unlock();
            }
        }
    }
}
