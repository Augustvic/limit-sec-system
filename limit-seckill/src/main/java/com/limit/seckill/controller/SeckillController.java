package com.limit.seckill.controller;

import com.limit.common.concurrent.BloomFilter;
import com.limit.common.extension.ExtensionLoader;
import com.limit.common.threadpool.ThreadPool;
import com.limit.common.threadpool.ThreadPoolConfig;
import com.limit.redis.seckill.GoodsKey;
import com.limit.redis.service.RedisService;
import com.limit.redis.service.RedissonService;
import com.limit.seckill.rocketmq.Sender;
import com.limit.user.access.AccessLimit;
import com.limit.user.entity.SeckillUser;
import com.limit.seckill.entity.SeckillGoods;
import com.limit.seckill.entity.SeckillOrder;
import com.limit.seckill.rocketmq.SeckillMessage;
import com.limit.common.result.CodeMsg;
import com.limit.common.result.Result;
import com.limit.seckill.service.GoodsService;
import com.limit.seckill.service.SeckillService;
import com.limit.seckill.service.OrderService;
import com.limit.common.utils.BaseUtil;
import com.limit.user.service.SeckillUserService;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    SeckillUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @Autowired
    Sender sender;

    @Autowired
    RedissonService redissonService;

    @Autowired
    BloomFilter bloomFilter;

    // 互斥锁的数量
    private static final int nLocks = 100;
    private static final String[] LOCKS = new String[nLocks];
    static {
        for (int i = 0; i < nLocks; i++) {
            LOCKS[i] = "lock" + i;
        }
    }

    private static final Map<Long, Boolean> localOverMap = new HashMap<>();

    private static final ScheduledExecutorService checkGoodsServiceExecutor = (ScheduledExecutorService) ExtensionLoader.getExtensionLoader(ThreadPool.class).getExtension("scheduled").getExecutor(new ThreadPoolConfig());;

    @PostConstruct
    private void init() {
        // 获取最近时间段内即将开始秒杀的商品
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 获取距现在 5 - 30 分钟时间段内即将开始秒杀的商品
                List<SeckillGoods> goodsList = seckillService.listSeckillGoodsLatest(5 * 60, 30 * 60);
                if (goodsList == null) {
                    return;
                }
                for (SeckillGoods goods : goodsList) {
                    // 秒杀结束 1 分钟后过期
                    int expireSeconds = BaseUtil.safeLongToInt((goods.getEndDate().getTime() - new Date().getTime()) / 1000 + 60);
                    redisService.set(new GoodsKey(expireSeconds, "gs"), "" + goods.getId(), goods.getStockCount());
                    localOverMap.put(goods.getId(), false);
                }
            }
        };
        // 20 分钟检查一次
        checkGoodsServiceExecutor.scheduleAtFixedRate(task, 0,
                20 * 60, TimeUnit.SECONDS);
    }

    /**
     * GET/POST 区别
     * GET幂等，从服务端获取数据
     * POST向服务端提交数据
     */
    @RequestMapping(value = "/{path}/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(Model model, SeckillUser user,
                                @RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path) throws Exception{
        model.addAttribute("user", user);
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        // 验证 path
        boolean check = seckillService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //判断是否已经秒杀到了
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        // 已经秒杀到了，不能重复秒杀
        if (order != null) {
            return Result.error(CodeMsg.REPEAT_MIAOSHA);
        }

        // 检查秒杀商品库存是否已经存入缓存，避免缓存击穿
        String[] locks = LOCKS;
        // 获取同一商品库存的线程中，只有一个线程能读取数据库，因为同一商品的 goodsId 相同
        // 如果不同的商品 index 相同，也需要等待释放锁
        // 在“库存”这个缓存里，所有的商品一共只有 locks.length 把锁
        int index = BaseUtil.safeLongToInt(goodsId & (locks.length - 1));
        RLock rLock = redissonService.getRLock(locks[index]);
        while (!redisService.exists(GoodsKey.getSeckillGoodsStock, "" + goodsId)) {
            if (rLock.tryLock(100, 10, TimeUnit.SECONDS)) {
                try {
                    // 再次检查缓存是否存在，避免上锁之前其他线程已经写入了缓存
                    if (!redisService.exists(GoodsKey.getSeckillGoodsStock, "" + goodsId)) {
                        SeckillGoods goods = seckillService.getSeckillGoodById(goodsId);
                        if (goods == null) {
                            return Result.error(CodeMsg.REQUEST_ILLEGAL);
                        }
                        // 秒杀结束一分钟之后缓存失效
                        int expireSeconds = BaseUtil.safeLongToInt((goods.getEndDate().getTime() - new Date().getTime()) / 1000 + 60);
                        redisService.set(new GoodsKey(expireSeconds, "gs"), "" + goods.getId(), goods.getStockCount());
                        localOverMap.put(goods.getId(), false);
                    }
                } finally {
                    rLock.unlock();
                }
            }
        }

        // 内存标记减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        // 预减库存
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        // 入队
        SeckillMessage mm = new SeckillMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);

        System.out.println("---------------------");
        System.out.println("send: " + Thread.currentThread().getName());
        System.out.println("---------------------");

        sender.sendSeckillMessage(mm);
        return Result.success(0); // 排队中
        /*
        //秒杀核心功能模块
        // 判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEAT_MIAOSHA);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = seckillService.seckill(user, goods);
        return Result.success(orderInfo);
        */
    }

    /**
     * orderId: 成功
     * -1：秒杀失败
     * 0：排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, SeckillUser user,
                                @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        long result = seckillService.getSeckillResult(user.getId(), goodsId);

        System.out.println("---------------------");
        System.out.println("result: " + Thread.currentThread().getName());
        System.out.println("---------------------");

        return Result.success(result);
    }

    // access拦截器拦截消息
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(HttpServletRequest request,
                                         SeckillUser user,
                                         @RequestParam("goodsId") long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        // 验证码验证
        boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check)
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        String path = seckillService.createSeckillPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillVerifyCode(HttpServletResponse response, SeckillUser user,
                                               @RequestParam("goodsId") long goodsId) {
        System.out.println("verifyCode:" + Thread.currentThread().getName());
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        try {
            BufferedImage image = seckillService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

}
