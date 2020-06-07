package com.limit.common.threadpool;

import com.limit.common.Constants;
import com.limit.common.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
public class ThreadPoolFactory implements Factory {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolFactory.class);
    private static final Map<String, ThreadPoolLoader> THREADPOOL_LOADERS = new ConcurrentHashMap<>();
    private static final Map<String, Executor> THREADPOOLS = new ConcurrentHashMap<>();
    private static final Map<Executor, String> THREADPOOL_NAME = new ConcurrentHashMap<>();
    private static final Set<String> TYPES = new HashSet<>();
    static {
        TYPES.add(Constants.COMMON_THREAD_POOL);
        TYPES.add(Constants.FIXED_THREAD_POOL);
        TYPES.add(Constants.CACHED_THREAD_POOL);
        TYPES.add(Constants.SCHEDULED_THREAD_POOL);
    }

    public Executor getThreadPool(String type, ThreadPoolConfig config) {
        Executor executor = THREADPOOLS.get(config.getName());
        if (executor == null) {
            if (!TYPES.contains(type)) {
                log.info("The type of " + type + "[" + config.getName() + "]" + " thread pool is not supported!");
                return null;
            } else {
                ThreadPoolLoader loader = THREADPOOL_LOADERS.get(type);
                if (loader == null) {
                    String realName = "com.limit.common.threadpool.loader." + type + "ThreadPoolLoader";
                    try {
                        loader = (ThreadPoolLoader) Class.forName(realName).newInstance();
                    } catch (Exception e) {
                        log.info(e.toString());
                    }
                    THREADPOOL_LOADERS.putIfAbsent(type, loader);
                    loader = THREADPOOL_LOADERS.get(type);
                }
                executor = loader.getExecutor(config);
                String name = config.getName();
                THREADPOOLS.putIfAbsent(name, executor);
                THREADPOOL_NAME.putIfAbsent(executor, name);
                executor = THREADPOOLS.get(name);
            }
        }
        return executor;
    }

    @Override
    public void destroy(Object obj) {
        if (obj instanceof Executor) {
            String name = THREADPOOL_NAME.remove(obj);
            THREADPOOLS.remove(name);
        }
    }

}
