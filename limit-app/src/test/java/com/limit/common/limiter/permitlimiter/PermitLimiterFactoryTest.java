package com.limit.common.limiter.permitlimiter;

import com.limit.MainApplication;
import com.limit.redis.service.RedisService;
import com.limit.redis.service.RedissonService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class PermitLimiterFactoryTest {

    @Autowired
    RedisService redisService;

    @Autowired
    RedissonService redissonService;

    @Test
    public void getPermitLimiter() {
        PermitLimiterFactory factory = new PermitLimiterFactory();
        PermitLimiterConfig config = new PermitLimiterConfig("testPermitLimiter", 1, 1000, redissonService.getRLock("testPermitLock"), redisService);
        PermitLimiter permitLimiter = factory.getPermitLimiter(config);
        if (permitLimiter.acquire()) {
            System.out.println("Main thread passed at first time.");
        } else {
            System.out.println("Main thread failed at first time.");
        }
        if (permitLimiter.acquire()) {
            System.out.println("Main thread passed at second time.");
        } else {
            System.out.println("Main thread failed at second time.");
        }
        System.out.println("Before first added: " + permitLimiter.getBucket().getStoredPermits());
        permitLimiter.addPermits(100);
        System.out.println("After added 100 permits: " + permitLimiter.getBucket().getStoredPermits());
        if (permitLimiter.acquire()) {
            System.out.println("Main thread passed at third time.");
        } else {
            System.out.println("Main thread failed at third time.");
        }
        if (permitLimiter.acquire()) {
            System.out.println("Main thread passed at forth time.");
        } else {
            System.out.println("Main thread failed at forth time.");
        }
        System.out.println("Before second added: " + permitLimiter.getBucket().getStoredPermits());
        permitLimiter.addPermits(500);
        System.out.println("After added 500 permits: " + permitLimiter.getBucket().getStoredPermits());
    }
}