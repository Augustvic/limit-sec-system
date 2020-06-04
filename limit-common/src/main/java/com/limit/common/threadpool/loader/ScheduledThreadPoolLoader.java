package com.limit.common.threadpool.loader;

import com.limit.common.Constants;
import com.limit.common.threadpool.support.AbortPolicy;
import com.limit.common.threadpool.support.NamedThreadFactory;
import com.limit.common.threadpool.support.ThreadPoolConfig;
import com.limit.common.threadpool.ThreadPoolLoader;

import java.util.concurrent.*;

public class ScheduledThreadPoolLoader implements ThreadPoolLoader {

    @Override
    public Executor getExecutor(ThreadPoolConfig config) {
        String name = config.getName();
        Integer corePoolSize =
                config.getCorePoolSize().equals(Constants.DEFAULT_CORE_THREADS)
                        ? 1 : config.getCorePoolSize();
        return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory(name), new AbortPolicy(name));
    }
}
