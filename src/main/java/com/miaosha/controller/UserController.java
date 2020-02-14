package com.miaosha.controller;

import com.miaosha.entity.MiaoshaUser;
import com.miaosha.redis.RedissonService;
import com.miaosha.result.Result;
import com.miaosha.util.MD5Util;
import com.miaosha.util.concurrent.RateLimiter;
import org.apache.kafka.common.metrics.stats.Count;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @RequestMapping("/test")
    public String test() {
        // Code
        return "login";
    }
}
