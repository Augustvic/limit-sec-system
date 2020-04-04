package com.limit.common.threadpool.common;

import com.limit.common.extension.ExtensionLoader;
import com.limit.common.threadpool.ThreadPool;
import com.limit.common.threadpool.ThreadPoolConfig;
import org.junit.Test;

import java.util.concurrent.ThreadPoolExecutor;

public class CommonThreadPoolTest {

    @Test
    public void getExecutor() {
        ThreadPoolConfig config = new ThreadPoolConfig();

        ThreadPoolExecutor fixed = (ThreadPoolExecutor) ExtensionLoader.getExtensionLoader(ThreadPool.class).getExtension("fixed").getExecutor(config);
        System.out.println(fixed.getCorePoolSize());
        System.out.println(fixed.getMaximumPoolSize());
        fixed.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName());
                System.out.println("Hello, Fixed Thread Pool!");
            }
        });
        fixed.shutdown();

        ThreadPoolExecutor cached = (ThreadPoolExecutor) ExtensionLoader.getExtensionLoader(ThreadPool.class).getExtension("cached").getExecutor(config);
        System.out.println(cached.getCorePoolSize());
        System.out.println(cached.getMaximumPoolSize());
        cached.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName());
                System.out.println("Hello, Cached Thread Pool!");
            }
        });
        cached.shutdown();
    }
}
