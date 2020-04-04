package com.limit.common.threadpool.fixed;

import com.limit.common.Constants;
import com.limit.common.threadpool.AbortPolicy;
import com.limit.common.threadpool.NamedThreadFactory;
import com.limit.common.threadpool.ThreadPool;
import com.limit.common.threadpool.ThreadPoolConfig;

import java.util.concurrent.*;

public class FixedThreadPool implements ThreadPool {

    @Override
    public Executor getExecutor(ThreadPoolConfig config) {
        String name = config.getName();
        Integer corePoolSize = config.getCorePoolSize();
        Integer queues = config.getQueues();
        BlockingQueue<Runnable> queue = queues == 0 ? new SynchronousQueue<Runnable>()
                : (queues < 0 ? new ArrayBlockingQueue<Runnable>(Math.abs(queues))
                : new LinkedBlockingQueue<Runnable>(queues));
        return new ThreadPoolExecutor(corePoolSize, corePoolSize, 0, TimeUnit.MILLISECONDS,
                queue, new NamedThreadFactory(name), new AbortPolicy(name));
    }
}
