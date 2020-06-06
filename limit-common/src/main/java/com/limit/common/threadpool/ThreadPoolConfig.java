package com.limit.common.threadpool;

import com.limit.common.Constants;

// 线程池配置
public class ThreadPoolConfig {
    // 线程池名字
    private final String name;
    // 核心线程数
    private int corePoolSize;
    // 最大线程数
    private int maximumPoolSize;
    // 阻塞队列容量
    private int queues;
    // 存活时间
    private int keepAliveTime;

    public ThreadPoolConfig(String name) {
        this(name, Constants.DEFAULT_CORE_THREADS,
                Constants.DEFAULT_MAX_THREADS, Constants.DEFAULT_BLOCKING_QUEUE,
                Constants.DEFAULT_ALIVE_TIME);
    }
    
    // scheduled
    public ThreadPoolConfig(String name, int corePoolSize) {
        this(name, corePoolSize, Constants.DEFAULT_MAX_THREADS,
                Constants.DEFAULT_BLOCKING_QUEUE, Constants.DEFAULT_ALIVE_TIME);
    }

    // common
    public ThreadPoolConfig(String name, int corePoolSize, int maximumPoolSize, int queues, int keepAliveTime) {
        this.name = name;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.queues = queues;
        this.keepAliveTime = keepAliveTime;
    }

    public String getName() {
        return name;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getQueues() {
        return queues;
    }

    public void setQueues(int queues) {
        this.queues = queues;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
}
