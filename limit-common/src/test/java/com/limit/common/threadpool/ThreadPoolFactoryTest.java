package com.limit.common.threadpool;

import com.limit.common.Constants;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolFactoryTest {

    @Test
    public void getFixedThreadPool() throws Exception{
        ThreadPoolFactory threadPoolFactory = new ThreadPoolFactory();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) threadPoolFactory.getThreadPool(Constants.FIXED_THREAD_POOL, new ThreadPoolConfig("test1"));
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

    @Test
    public void getLimitedThreadPool() throws Exception{
        ThreadPoolFactory threadPoolFactory = new ThreadPoolFactory();
        Object executor = threadPoolFactory.getThreadPool("Limited", new ThreadPoolConfig("test2"));
        System.out.println(executor == null); // true
    }
}