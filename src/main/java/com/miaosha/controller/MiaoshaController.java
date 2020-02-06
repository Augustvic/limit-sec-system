package com.miaosha.controller;


import com.miaosha.access.AccessLimit;
import com.miaosha.config.ThreadPoolConfig;
import com.miaosha.entity.MiaoshaGoods;
import com.miaosha.entity.MiaoshaOrder;
import com.miaosha.entity.MiaoshaUser;
import com.miaosha.kafka.MQSender;
import com.miaosha.kafka.MiaoshaMessage;
import com.miaosha.redis.GoodsKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaService;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.service.OrderService;
import com.miaosha.util.BaseUtil;
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
@RequestMapping("/miaosha")
public class MiaoshaController {

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

    @Autowired
    MQSender sender;

    private Map<Long, Boolean> localOverMap = new HashMap<>();

    @PostConstruct
    private void init() {
        ScheduledExecutorService checkGoodsServiceExecutor = ThreadPoolConfig.checkGoodsServiceExecutor();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 获取距现在 30 - 60 分钟时间段内即将开始秒杀的商品
                List<MiaoshaGoods> goodsList = miaoshaService.listMiaoshaGoodsLatest(30 * 60, 60 * 60);
                if (goodsList == null) {
                    return;
                }
                System.out.println(goodsList.toString());
                for (MiaoshaGoods goods : goodsList) {
                    // 秒杀结束 1 分钟后过期
                    int expireSeconds = BaseUtil.safeIntToLong((goods.getEndDate().getTime() - new Date().getTime()) / 1000 + 60);
                    redisService.set(new GoodsKey(expireSeconds, "gs"), "" + goods.getId(), goods.getStockCount());
                    localOverMap.put(goods.getId(), false);
                }
            }
        };
        // 30 分钟检查一次
        checkGoodsServiceExecutor.scheduleAtFixedRate(task, 0,
                30 * 60, TimeUnit.SECONDS);
    }

    /**
     * GET/POST 区别
     * GET幂等，从服务端获取数据
     * POST向服务端提交数据
     */
    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(Model model, MiaoshaUser user,
                                @RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        // 验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        // 内存标记减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        // 预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        // 判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEAT_MIAOSHA);
        }
        // 入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
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
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEAT_MIAOSHA);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
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
    public Result<Long> miaoshaResult(Model model, MiaoshaUser user,
                                @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

    // access拦截器拦截消息
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request,
                                         MiaoshaUser user,
                                         @RequestParam("goodsId") long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        // 验证码验证
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check)
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        String path = miaoshaService.createMiaoshaPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, MiaoshaUser user,
                                               @RequestParam("goodsId") long goodsId) {
        System.out.println("verifyCode:" + Thread.currentThread().getName());
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        try {
            BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
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
