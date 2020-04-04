package com.limit.common.threadpool.common;

import com.limit.common.threadpool.AbortPolicy;
import com.limit.common.threadpool.NamedThreadFactory;
import com.limit.common.threadpool.ThreadPool;
import com.limit.common.threadpool.ThreadPoolConfig;

import java.util.concurrent.*;

public class CommonThreadPool implements ThreadPool {

    @Override
    public Executor getExecutor(ThreadPoolConfig config) {
        String name = config.getName();
        Integer corePoolSize = config.getCorePoolSize();
        Integer maximumPoolSize = config.getMaximumPoolSize();
        Integer queues = config.getQueues();
        BlockingQueue<Runnable> queue = queues == 0 ? new SynchronousQueue<Runnable>()
                : (queues < 0 ? new ArrayBlockingQueue<Runnable>(Math.abs(queues))
                : new LinkedBlockingQueue<Runnable>(queues));
        Integer keepAliveTime = config.getKeepAliveTime();
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
                queue, new NamedThreadFactory(name), new AbortPolicy(name));
    }
}
