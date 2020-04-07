package com.limit.common.threadpool;

import com.limit.common.Constants;

// 线程池配置
public class ThreadPoolConfig {
    // 线程池名字
    private String name = Constants.THREAD_POOL_NAME;
    // 核心线程数
    private Integer corePoolSize = Constants.DEFAULT_CORE_THREADS;
    // 最大线程数
    private Integer maximumPoolSize = Constants.DEFAULT_MAX_THREADS;
    // 阻塞队列
    // 等于 0：SynchronousQueue
    // 小于 0：ArrayBlockingQueue，绝对值为容量
    // 大于 0：LinkedBlockingQueue，绝对值为容量
    private Integer queues = Constants.DEFAULT_BLOCKING_QUEUE;
    // 存活时间
    private Integer keepAliveTime = Constants.DEFAULT_ALIVE_TIME;

    public ThreadPoolConfig() {}

    public ThreadPoolConfig(String name) {
        this.name = name;
    }

    // scheduled
    public ThreadPoolConfig(String name, Integer corePoolSize) {
        this.name = name;
        this.corePoolSize = corePoolSize;
    }

    // common
    public ThreadPoolConfig(String name, Integer corePoolSize, Integer maximumPoolSize, Integer queues, Integer keepAliveTime) {
        this.name = name;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.queues = queues;
        this.keepAliveTime = keepAliveTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public Integer getQueues() {
        return queues;
    }

    public void setQueues(Integer queues) {
        this.queues = queues;
    }

    public Integer getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(Integer keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
}
