package com.limit.common.threadpool;

import java.util.concurrent.*;

public class ThreadPoolConfig2 {

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

    // 线程池
    private final static ScheduledExecutorService checkGoodsServiceExecutor =
            new ScheduledThreadPoolExecutor(1,  REJECTED_EXECUTION_HANDLER);

    public ExecutorService asyncServiceExecutor() {
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS, WORK_QUEUE,
                REJECTED_EXECUTION_HANDLER);
    }

    /**
     * 用于定时检查即将参加秒杀的商品是否在缓存内的线程池，核心线程数为 1。
     * 将商品加入缓存中时，设置过期时间为秒杀时间段
     * @return ExecutorService 定时线程池
     */
    public static ScheduledExecutorService checkGoodsServiceExecutor() {
        return checkGoodsServiceExecutor;
    }
}
