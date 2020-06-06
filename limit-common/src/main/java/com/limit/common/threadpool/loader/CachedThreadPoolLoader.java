package com.limit.common.threadpool.loader;

import com.limit.common.threadpool.queue.ResizableLinkedBlockingQueue;
import com.limit.common.threadpool.AbortPolicy;
import com.limit.common.threadpool.CommonThreadPoolExecutor;
import com.limit.common.threadpool.NamedThreadFactory;
import com.limit.common.threadpool.ThreadPoolConfig;
import com.limit.common.threadpool.ThreadPoolLoader;

import java.util.concurrent.*;

public class CachedThreadPoolLoader implements ThreadPoolLoader {

    @Override
    public Executor getExecutor(ThreadPoolConfig config) {
        String name = config.getName();
        int queues = config.getQueues();
        int keepAliveTime = config.getKeepAliveTime();
        return new CommonThreadPoolExecutor(0, Integer.MAX_VALUE, keepAliveTime, TimeUnit.MILLISECONDS,
                new ResizableLinkedBlockingQueue<Runnable>(queues), new NamedThreadFactory(name), new AbortPolicy(name));
    }
}
