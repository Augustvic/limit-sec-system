package com.limit.common.threadpool;

import com.limit.MainApplication;
import com.limit.common.threadpool.support.ThreadPoolConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class ThreadPoolFactoryTest {

    @Autowired
    ThreadPoolFactory threadPoolFactory;

    @Test
    public void getFixedThreadPool() throws Exception{
        ThreadPoolExecutor executor = (ThreadPoolExecutor) threadPoolFactory.getFixedThreadPool(new ThreadPoolConfig());
        final CountDownLatch latch = new CountDownLatch(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Hello, Fixed Thread Pool.");
                latch.countDown();
            }
        });
        latch.await();
        executor.shutdown();
    }
}