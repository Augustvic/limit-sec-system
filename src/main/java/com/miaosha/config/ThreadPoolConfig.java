package com.miaosha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {

    // CPU 核数
    private final static int CPUS = Runtime.getRuntime().availableProcessors();
    // 默认核心线程数
    private final static int CORE_POOL_SIZE = CPUS + 1;
    // 默认最大线程数
    private final static int MAX_POOL_SIZE = (CPUS + 1) * 2;
    // 默认阻塞队列
    private final static BlockingQueue<Runnable> WORK_QUEUE
            = new LinkedBlockingQueue<>();
    // 默认存活时间
    private final static int KEEP_ALIVE_TIME = 0;
    // 默认拒绝策略
    private final static RejectedExecutionHandler REJECTED_EXECUTION_HANDLER
            = new ThreadPoolExecutor.DiscardPolicy();

    @Bean
    public ExecutorService asyncServiceExecutor() {
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS, WORK_QUEUE,
                REJECTED_EXECUTION_HANDLER);
    }
}
