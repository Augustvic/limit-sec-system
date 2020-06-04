package com.limit.common.threadpool.loader;

import com.limit.common.threadpool.queue.ResizableLinkedBlockingQueue;
import com.limit.common.threadpool.support.AbortPolicy;
import com.limit.common.threadpool.support.CommonThreadPoolExecutor;
import com.limit.common.threadpool.support.NamedThreadFactory;
import com.limit.common.threadpool.support.ThreadPoolConfig;
import com.limit.common.threadpool.ThreadPoolLoader;

import java.util.concurrent.*;

public class CommonThreadPoolLoader implements ThreadPoolLoader {

    @Override
    public Executor getExecutor(ThreadPoolConfig config) {
        String name = config.getName();
        Integer corePoolSize = config.getCorePoolSize();
        Integer maximumPoolSize = config.getMaximumPoolSize();
        Integer queues = config.getQueues();
        Integer keepAliveTime = config.getKeepAliveTime();
        return new CommonThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
                new ResizableLinkedBlockingQueue<>(queues), new NamedThreadFactory(name), new AbortPolicy(name));
    }
}
