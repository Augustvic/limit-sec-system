package com.limit.common.threadpool.scheduled;

import com.limit.common.threadpool.AbortPolicy;
import com.limit.common.threadpool.NamedThreadFactory;
import com.limit.common.threadpool.ThreadPool;
import com.limit.common.threadpool.ThreadPoolConfig;

import java.util.concurrent.*;

public class ScheduledThreadPool implements ThreadPool {

    @Override
    public Executor getExecutor(ThreadPoolConfig config) {
        String name = config.getName();
        Integer corePoolSize = config.getCorePoolSize();
        return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory(name), new AbortPolicy(name));
    }
}
