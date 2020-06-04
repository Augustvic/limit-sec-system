package com.limit.common.threadpool.loader;

import com.limit.common.threadpool.queue.ResizableLinkedBlockingQueue;
import com.limit.common.threadpool.support.AbortPolicy;
import com.limit.common.threadpool.support.CommonThreadPoolExecutor;
import com.limit.common.threadpool.support.NamedThreadFactory;
import com.limit.common.threadpool.support.ThreadPoolConfig;
import com.limit.common.threadpool.ThreadPoolLoader;

import java.util.concurrent.*;

public class FixedThreadPoolLoader implements ThreadPoolLoader {

    @Override
    public Executor getExecutor(ThreadPoolConfig config) {
        String name = config.getName();
        Integer corePoolSize = config.getCorePoolSize();
        Integer queues = config.getQueues();
        return new CommonThreadPoolExecutor(corePoolSize, corePoolSize, 0, TimeUnit.MILLISECONDS,
                new ResizableLinkedBlockingQueue<Runnable>(queues), new NamedThreadFactory(name), new AbortPolicy(name));
    }
}
