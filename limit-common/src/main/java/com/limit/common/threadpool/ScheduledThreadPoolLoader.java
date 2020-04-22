package com.limit.common.threadpool;

import com.limit.common.Constants;

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
