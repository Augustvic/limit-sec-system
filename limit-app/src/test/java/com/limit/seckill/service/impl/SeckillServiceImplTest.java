package com.limit.seckill.service.impl;

import com.limit.MainApplication;
import com.limit.common.result.Result;
import com.limit.seckill.exchange.DefaultFuture;
import com.limit.seckill.service.SeckillService;
import com.limit.user.entity.SeckillUser;
import com.limit.user.service.SeckillUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class SeckillServiceImplTest {

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    SeckillServiceImpl seckillService;

    @Test
    public void doSeckill() {
        final int nThreads = 1;
        final int nTasks = 2;
        final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        final CountDownLatch latch = new CountDownLatch(nTasks);
        final SeckillUser user = seckillUserService.getById(18716558336L);
        final long goodsId = 1L;
        final AtomicLong time = new AtomicLong(0);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    Result<Long> result = seckillService.doSeckill(user, goodsId);
                    if (result.getData() != null) {
                        long requestId = result.getData();
                        while (DefaultFuture.get(requestId) != 0L) {
                            Thread.yield();
                        }
                    }
                    long endTime = System.currentTimeMillis();
                    long bet = endTime - startTime;
                    time.getAndAdd(bet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        };
        for (int i = 0; i < nTasks; i++) {
            executor.execute(task);
        }
        try {
            latch.await();
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Time: " + time);
    }
}