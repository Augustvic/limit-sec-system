package com.limit.seckill.service.impl;

import com.limit.common.Constants;
import com.limit.common.cache.CacheFactory;
import com.limit.common.cache.LRU2Cache;
import com.limit.common.concurrent.permitlimiter.PermitLimiter;
import com.limit.common.concurrent.permitlimiter.PermitLimiterConfig;
import com.limit.common.concurrent.permitlimiter.PermitLimiterFactory;
import com.limit.common.result.CodeMsg;
import com.limit.common.result.Result;
import com.limit.common.threadpool.ThreadPoolFactory;
import com.limit.common.threadpool.ThreadPoolConfig;
import com.limit.redis.key.seckill.GoodsKey;
import com.limit.redis.key.seckill.SeckillKey;
import com.limit.redis.lock.DLock;
import com.limit.redis.lock.LockFactory;
import com.limit.redis.service.RedisService;
import com.limit.redis.service.RedissonService;
import com.limit.rocketmq.producer.ProducerFactory;
import com.limit.seckill.dao.SeckillGoodsDao;
import com.limit.seckill.entity.SeckillGoods;
import com.limit.seckill.entity.OrderInfo;
import com.limit.common.utils.BaseUtil;
import com.limit.common.utils.MD5Util;
import com.limit.common.utils.UUIDUtil;
import com.limit.seckill.exchange.DefaultFuture;
import com.limit.seckill.exchange.message.Request;
import com.limit.seckill.exchange.message.Response;
import com.limit.seckill.rocketmq.SeckillConsumer;
import com.limit.seckill.rocketmq.SeckillConsumerFactory;
import com.limit.seckill.rocketmq.SeckillProducer;
import com.limit.seckill.service.SeckillService;
import com.limit.seckill.vo.GoodsVo;
import com.limit.user.entity.SeckillUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    SeckillGoodsDao seckillGoodsDao;

    @Autowired
    GoodsServiceImpl goodsService;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    RedisService redisService;

    @Autowired
    RedissonService redissonService;

    @Autowired
    ThreadPoolFactory threadPoolFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    PermitLimiterFactory permitLimiterFactory;

    @Autowired
    ProducerFactory producerFactory;

    @Autowired
    SeckillConsumerFactory consumerFactory;

//    private final Map<Long, Boolean> localOverMap = new ConcurrentHashMap<>();
    private LRU2Cache<Long, Boolean> localStockOver;
    private ScheduledExecutorService checkGoodsServiceExecutor;
    private DLock getStockLock;
    private DLock reduceStockLock;
    private PermitLimiter readDBPermitLimiter;
    private PermitLimiter writeDBPermitLimiter;

    private SeckillProducer producer;
    private SeckillConsumer consumer;

    @PostConstruct
    private void init() {
        checkGoodsServiceExecutor = (ScheduledExecutorService) threadPoolFactory.getThreadPool(Constants.SCHEDULED_THREAD_POOL, new ThreadPoolConfig("LoadInventory"));

        this.readDBPermitLimiter = permitLimiterFactory.getPermitLimiter(
                new PermitLimiterConfig("SeckillReadDBRateLimiter", redissonService.getRLock("readlock"), redisService));
        this.writeDBPermitLimiter = permitLimiterFactory.getPermitLimiter(
                new PermitLimiterConfig("SeckillWriteDBRateLimiter", redissonService.getRLock("writelock"), redisService));;
        this.getStockLock = LockFactory.getDLock("GetStock", Constants.N_LOCKS, redissonService);
        this.reduceStockLock = LockFactory.getDLock("ReduceStock", Constants.N_LOCKS, redissonService);

        this.localStockOver = (LRU2Cache) cacheFactory.getCache("LocalStockOver", Constants.LRU2_CACHE, Constants.CACHE_MAX_CAPACITY);

        this.producer = (SeckillProducer) producerFactory.getProducer("seckill_producer", SeckillProducer.path);
        this.consumer = consumerFactory.getSeckillConsumer("seckill_consumer");
        this.consumer.start();

        // 获取最近时间段内即将开始秒杀的商品
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 获取距现在 5 - 30 分钟时间段内即将开始秒杀的商品
                List<SeckillGoods> goodsList = listSeckillGoodsLatest(5 * 60, 30 * 60);
                if (goodsList == null) {
                    return;
                }
                for (SeckillGoods goods : goodsList) {
                    // 秒杀结束 1 分钟后过期
                    int expireSeconds = BaseUtil.safeLongToInt((goods.getEndDate().getTime() - new Date().getTime()) / 1000 + 60);
                    redisService.set(new GoodsKey(expireSeconds, "gs"), "" + goods.getId(), goods.getStockCount());
                    localStockOver.put(goods.getId(), false);
                }
            }
        };
        // 20 分钟检查一次
        checkGoodsServiceExecutor.scheduleAtFixedRate(task, 0,
                20 * 60, TimeUnit.SECONDS);
    }

//    public CodeMsg preReduceInventory(SeckillUser user, long goodsId) throws Exception{
//        //判断是否已经秒杀到了
//        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
//        // 已经秒杀到了，不能重复秒杀
//        if (order != null) {
//            return CodeMsg.REPEAT_MIAOSHA;
//        }
//
//        // 检查秒杀商品库存是否已经存入缓存，避免缓存击穿
//        // 获取同一商品库存的线程中，只有一个线程能读取数据库，因为同一商品的 goodsId 相同
//        // 如果不同的商品 index 相同，也需要等待释放锁
//        // 在“库存”这个缓存里，所有的商品一共只有 locks.length 把锁
//        int key = BaseUtil.safeLongToInt(goodsId & (N_LOCKS - 1));
//        while (!redisService.exists(GoodsKey.getSeckillGoodsStock, "" + goodsId)) {
//            if (dLock.lock(key)) {
//                try {
//                    // 再次检查缓存是否存在，避免上锁之前其他线程已经写入了缓存
//                    if (!redisService.exists(GoodsKey.getSeckillGoodsStock, "" + goodsId)) {
//                        SeckillGoods goods = getSeckillGoodById(goodsId);
//                        if (goods == null) {
//                            return CodeMsg.REQUEST_ILLEGAL;
//                        }
//                        // 秒杀结束一分钟之后缓存失效
//                        int expireSeconds = BaseUtil.safeLongToInt((goods.getEndDate().getTime() - new Date().getTime()) / 1000 + 60);
//                        redisService.set(new GoodsKey(expireSeconds, "gs"), "" + goods.getId(), goods.getStockCount());
//                        localOverMap.put(goods.getId(), false);
//                    }
//                } finally {
//                    dLock.unlock(key);
//                }
//            }
//            else {
//                Thread.yield();
//            }
//        }
//        // 内存标记减少redis访问
//        boolean over = localOverMap.get(goodsId);
//        if (over) {
//            return CodeMsg.MIAO_SHA_OVER;
//        }
//        // 预减库存
//        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
//        if (stock < 0) {
//            localOverMap.put(goodsId, true);
//            return CodeMsg.MIAO_SHA_OVER;
//        }
//
//        return CodeMsg.SUCCESS;
//    }


    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
        // 减库存 下订单 写入秒杀订单
        boolean success = reduceStock(goods);
        if (success) {
            // order_info, seckill_order
            return orderService.createOrder(user, goods);
        }
        else {
            // 秒杀结束
            setGoodsOver(goods.getId());
            return null;
        }
    }

//    public long getSeckillResult(Long userId, long goodsId) {
//        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(userId, goodsId);
//        // 秒杀成功
//        if (order != null) {
//            return order.getOrderId();
//        }
//        else {
//            boolean isOver = getGoodsOver(goodsId);
//            if (isOver)
//                return -1;
//            else
//                return 0;
//        }
//    }

    //redis中标记商品已经卖完
    public void setGoodsOver(Long goodsId) {
        redisService.set(SeckillKey.isGoodsOver, "" + goodsId, true);
    }

    public boolean getGoodsOver(long goodsId) {
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

    public int calc(String exp) {
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


//    /**
//     * 接收到消息之后，进行后续检查库存，写订单等操作
//     * @param request 接收到的消息
//     */
//    public void afterReceiveRequest(Request request) throws Exception{
//
//        SeckillUser user = request.getUser();
//        long goodsId = request.getGoodsId();
//
//        // 如果没有获取到读数据库令牌
//        if (!readDBPermitLimiter.acquireMillis()) {
//            // 重新进入队列等待
//            producer.sendSeckillRequest(request);
//            return;
//        }
//        // 读取数据库中商品库存
//        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//        int stock = goods.getStockCount();
//        if (stock <= 0)
//            return;
//        //判断是否已经秒杀到了
//        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
//        // 已经秒杀到了，不能重复秒杀
//        if (order != null) {
//            return;
//        }
//        // 如果没有获取到写数据库令牌
//        if (!writeDBPermitLimiter.acquireMillis()) {
//            // 重新进入队列等待
//            producer.sendSeckillRequest(request);
//            return;
//        }
//
//        //减库存 下订单 写入秒杀订单
//        // 获取互斥锁
//        RLock rLock = redissonService.getRLock("decrStockLock_" + goodsId);
//        // 最多等待 100 秒，上锁 10 秒后解锁
//        if (rLock.tryLock(100, 10, TimeUnit.SECONDS)) {
//            try {
//                OrderInfo orderInfo = rocketmq(user, goods);
//                if (orderInfo != null) {
//                    Response response = new Response(request.getId(), orderInfo.getId());
//                    DefaultFuture.received(response);
//                }
//            } finally {
//                rLock.unlock();
//            }
//        }
//    }

    public Result<Long> doSeckill(SeckillUser user, long goodsId) {
        CodeMsg preReduceResultMsg = CodeMsg.FAIL;
        // 预减库存
        try {
            preReduceResultMsg = preReduceInventory(user, goodsId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (preReduceResultMsg != CodeMsg.SUCCESS) {
            return Result.error(preReduceResultMsg);
        }

        Request request = new Request(user, goodsId);
        DefaultFuture future = new DefaultFuture(request.getId(), request, 20000);
        DefaultFuture.FUTURES.put(request.getId(), future);

        try {
            producer.sendOneWay(request.toString());
        } catch (Exception e) {
            future.cancel();
            e.printStackTrace();
            return Result.error(CodeMsg.FAIL);
        }
        return Result.success(request.getId()); // 排队中
    }


    // 用于测试
    //-----------------------------------------

    /**
     * 接收到消息之后，进行后续检查库存，写订单等操作
     * @param request 接收到的消息
     */
    public void afterReceiveRequest(Request request) throws Exception{
        System.out.println("afterReceiveRequest: " + Thread.currentThread().getName());

        SeckillUser user = request.getUser();
        long goodsId = request.getGoodsId();

        // 获取读数据库令牌
        if (!readDBPermitLimiter.acquireTillSuccess(1L, 10000L)) {
            Response response = new Response(request.getId(), -1L);
            DefaultFuture.received(response);
        }

        // 读取数据库中商品库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0)
            return;

        // 如果没有获取到写数据库令牌
        if (!writeDBPermitLimiter.acquireTillSuccess(1L, 10000L)) {
            Response response = new Response(request.getId(), -1L);
            DefaultFuture.received(response);
        }

        //减库存 下订单 写入秒杀订单
        // 获取互斥锁
        int key = BaseUtil.safeLongToInt(goodsId & (Constants.N_LOCKS - 1));
//        RLock rLock = redissonService.getRLock("decrStockLock_" + goodsId);
        if (reduceStockLock.lock(key)) {
            try {
                OrderInfo orderInfo = seckill(user, goods);
                if (orderInfo != null) {
                    Response response = new Response(request.getId(), orderInfo.getId());
                    DefaultFuture.received(response);
                }
            } finally {
                reduceStockLock.unlock(key);
            }
        }
    }

    public CodeMsg preReduceInventory(SeckillUser user, long goodsId) throws Exception{
        // 检查秒杀商品库存是否已经存入缓存，避免缓存击穿
        // 获取同一商品库存的线程中，只有一个线程能读取数据库，因为同一商品的 goodsId 相同
        // 如果不同的商品 index 相同，也需要等待释放锁
        // 在“库存”这个缓存里，所有的商品一共只有 locks.length 把锁
        int key = BaseUtil.safeLongToInt(goodsId & (Constants.N_LOCKS - 1));
        while (!redisService.exists(GoodsKey.getSeckillGoodsStock, "" + goodsId)) {
            if (getStockLock.lock(key)) {
                try {
                    // 再次检查缓存是否存在，避免上锁之前其他线程已经写入了缓存
                    if (!redisService.exists(GoodsKey.getSeckillGoodsStock, "" + goodsId)) {
                        SeckillGoods goods = getSeckillGoodById(goodsId);
                        if (goods == null) {
                            return CodeMsg.REQUEST_ILLEGAL;
                        }
                        // 秒杀结束一分钟之后缓存失效
                        int expireSeconds = BaseUtil.safeLongToInt((goods.getEndDate().getTime() - new Date().getTime()) / 1000 + 60);
                        redisService.set(new GoodsKey(expireSeconds, "gs"), "" + goods.getId(), goods.getStockCount());
                        localStockOver.put(goods.getId(), goods.getStockCount() < 0);
                    }
                } finally {
                    getStockLock.unlock(key);
                }
            }
            else {
                Thread.yield();
            }
        }
        // 内存标记减少redis访问
        Boolean over = localStockOver.get(goodsId);
        if (over != null && over) {
            return CodeMsg.MIAO_SHA_OVER;
        }
        // 预减库存
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
        if (stock < 0) {
            localStockOver.put(goodsId, true);
            return CodeMsg.MIAO_SHA_OVER;
        } else {
            localStockOver.put(goodsId, false);
        }
        return CodeMsg.SUCCESS;
    }
}
