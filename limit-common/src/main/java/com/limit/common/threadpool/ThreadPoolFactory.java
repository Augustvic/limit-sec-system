package com.limit.common.threadpool;

import com.limit.common.threadpool.support.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
public class ThreadPoolFactory {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolFactory.class);
    private static final Map<String, ThreadPoolLoader> THREADPOOL_LOADERS = new ConcurrentHashMap<>();

    public Executor getCommonThreadPool(ThreadPoolConfig config) {
        String name = "Common";
        return getThreadPoolLoader(name).getExecutor(config);
    }

    public Executor getCachedThreadPool(ThreadPoolConfig config) {
        String name = "Cached";
        return getThreadPoolLoader(name).getExecutor(config);
    }

    public Executor getFixedThreadPool(ThreadPoolConfig config) {
        String name = "Fixed";
        return getThreadPoolLoader(name).getExecutor(config);
    }

    public Executor getScheduledThreadPool(ThreadPoolConfig config) {
        String name = "Scheduled";
        return getThreadPoolLoader(name).getExecutor(config);
    }

    private ThreadPoolLoader getThreadPoolLoader(String name) {
        ThreadPoolLoader loader = THREADPOOL_LOADERS.get(name);
        if (loader == null) {
            String realName = "com.limit.common.threadpool.loader." + name + "ThreadPoolLoader";
            try {
                loader = (ThreadPoolLoader)Class.forName(realName).newInstance();
            } catch (Exception e) {
                log.info(e.toString());
            }
            THREADPOOL_LOADERS.put(name, loader);
            loader = THREADPOOL_LOADERS.get(name);
        }
        return loader;
    }
}
