package com.miaosha.controller;

import com.miaosha.entity.MiaoshaUser;
import com.miaosha.redis.RedissonService;
import com.miaosha.result.Result;
import com.miaosha.util.concurrent.RateLimiter;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(Model model, MiaoshaUser user) {
        return Result.success(user);
    }

    @Autowired
    RateLimiter rateLimiter;

    @RequestMapping("/test")
    public String test() {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        final CountDownLatch latch = new CountDownLatch(100);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (rateLimiter.acquire()) {
                    System.out.println(Thread.currentThread().getName() + "get");
                }
                else {
                    System.out.println(Thread.currentThread().getName() + "fail");
                }
                latch.countDown();
            }
        };
        for (int i = 0; i < 100; i++) {
            executor.execute(task);
        }
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
        return "login";
    }
}
