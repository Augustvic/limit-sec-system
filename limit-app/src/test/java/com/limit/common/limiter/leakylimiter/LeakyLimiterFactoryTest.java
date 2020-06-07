package com.limit.common.limiter.leakylimiter;

import com.limit.MainApplication;
import com.limit.redis.service.RedisService;
import com.limit.redis.service.RedissonService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class LeakyLimiterFactoryTest {

    @Autowired
    RedisService redisService;

    @Autowired
    RedissonService redissonService;

    @Test
    public void getLeakyLimiter() {
        LeakyLimiterFactory factory = new LeakyLimiterFactory();
        LeakyLimiterConfig config = new LeakyLimiterConfig("testLeakyLimiter", 1, redissonService.getRLock("testLeakylock"), redisService);
        final LeakyLimiter leakyLimiter = factory.getLeakyLimiter(config);
        final int N = 3;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (leakyLimiter.acquire()) {
                    System.out.println(Thread.currentThread().getName() + " passed.");
                } else {
                    System.out.println(Thread.currentThread().getName() + " failed.");
                }
            }
        };
        Executor executor = Executors.newFixedThreadPool(N);
        for (int i = 0; i < N; i++) {
            executor.execute(task);
        }
        try {
            Thread.sleep(2 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Executor executor2 = Executors.newFixedThreadPool(N);
        for (int i = 0; i < N; i++) {
            executor2.execute(task);
        }
    }
}