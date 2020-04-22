package com.limit.common.threadpool;

import java.util.concurrent.*;

public class CachedThreadPoolLoader implements ThreadPoolLoader {

    @Override
    public Executor getExecutor(ThreadPoolConfig config) {
        String name = config.getName();
        Integer queues = config.getQueues();
        BlockingQueue<Runnable> queue = queues == 0 ? new SynchronousQueue<Runnable>()
                : (queues < 0 ? new ArrayBlockingQueue<Runnable>(Math.abs(queues))
                : new LinkedBlockingQueue<Runnable>(queues));
        Integer keepAliveTime = config.getKeepAliveTime();
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, keepAliveTime, TimeUnit.MILLISECONDS,
                queue, new NamedThreadFactory(name), new AbortPolicy(name));
    }
}
