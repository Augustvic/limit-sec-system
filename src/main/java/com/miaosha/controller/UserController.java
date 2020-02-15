package com.miaosha.controller;

import com.google.common.hash.Funnels;
import com.miaosha.entity.MiaoshaUser;
import com.miaosha.redis.RedissonService;
import com.miaosha.result.Result;
import com.miaosha.util.MD5Util;
import com.miaosha.util.concurrent.BloomFilter;
import com.miaosha.util.concurrent.RateLimiter;
import org.apache.kafka.common.metrics.stats.Count;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(Model model, MiaoshaUser user) {
        return Result.success(user);
    }

    @Autowired
    BloomFilter bloomFilter;

    @RequestMapping("/test")
    public String test() {
        // Code
        int capacity = 1000;
        String where = "test";
        for (int i = 0; i < capacity; i++) {
            bloomFilter.put(where, String.valueOf(i));
        }
        int sum = 0;
        for (int i = capacity + 2000; i < capacity + 3000; i++) {
            if (bloomFilter.isExist(where, String.valueOf(i))) {
                sum ++;
            }
        }
        //0.03
        DecimalFormat df=new DecimalFormat("0.00");//设置保留位数
        System.out.println("错判率为:" + df.format((float)sum/10000));
        return "login";
    }
}
