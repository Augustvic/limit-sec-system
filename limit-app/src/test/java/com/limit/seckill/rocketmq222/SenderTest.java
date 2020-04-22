package com.limit.seckill.rocketmq222;

import com.limit.MainApplication;
import com.limit.seckill.exchange.message.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class SenderTest {

    @Test
    public void sendSeckillRequest() throws Exception{
        final int nThreads = 100;
        final Request request = new Request();
        final CountDownLatch latch = new CountDownLatch(nThreads);
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
//                    sender.sendSeckillRequest(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        };
        for (int i = 0; i < 100; i++) {
            executor.execute(task);
        }
        Thread.sleep(10000);
        latch.await();
    }
}