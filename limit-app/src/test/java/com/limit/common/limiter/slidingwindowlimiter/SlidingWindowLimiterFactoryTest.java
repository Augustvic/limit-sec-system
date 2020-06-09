package com.limit.common.limiter.slidingwindowlimiter;

import com.alibaba.fastjson.JSON;
import com.limit.MainApplication;
import com.limit.common.limiter.slidingwindow.SlidingWindowLimiter;
import com.limit.common.limiter.slidingwindow.SlidingWindowLimiterConfig;
import com.limit.common.limiter.slidingwindow.SlidingWindowLimiterFactory;
import com.limit.redis.service.RedisService;
import com.limit.redis.service.RedissonService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class SlidingWindowLimiterFactoryTest {

    @Autowired
    RedisService redisService;

    @Autowired
    RedissonService redissonService;

    private static Map<Boolean, String> map = new ConcurrentHashMap<>();
    static {
        map.putIfAbsent(true, "passed");
        map.putIfAbsent(false, "failed");
    }

    @Test
    public void getSlidingWindowLimiter() {
        SlidingWindowLimiterFactory factory = new SlidingWindowLimiterFactory();
        SlidingWindowLimiterConfig config = new SlidingWindowLimiterConfig("testSlidingWindowLimiter", 2, 10, redissonService.getRLock("testWindowLock"), redisService);
        SlidingWindowLimiter limiter = factory.getSlidingWindowLimiter(config);
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at first time."); // passed
        try {
            System.out.println("After sleep 500 millis--------");
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at second time."); // passed
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at third time."); // failed
        try {
            System.out.println("After sleep 600 millis--------");
            Thread.sleep(600);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at forth time."); // passed
        try {
            System.out.println("After sleep 500 millis--------");
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at fifth time."); // passed
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at sixth time."); // failed
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at seventh time."); // failed
        try {
            System.out.println("After sleep 1100 millis--------");
            Thread.sleep(1100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at eighth time."); // passed
        System.out.println("Main thread " + map.get(limiter.acquire()) + " at ninth time."); //passed
    }
}