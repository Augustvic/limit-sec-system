package com.limit.common.threadpool;

import com.limit.common.utils.BaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

// 记录在日志中，然后直接抛出异常
public class AbortPolicy extends ThreadPoolExecutor.AbortPolicy{

    protected static final Logger logger = LoggerFactory.getLogger(AbortPolicy.class);

    private final String threadPoolName;

    public AbortPolicy(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String now = BaseUtil.df.format(System.currentTimeMillis());
        String msg = String.format("Thread pool is EXHAUSTED!" +
                        " Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d)," +
                        " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s), in %s!",
                threadPoolName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating(), now);
        logger.warn(msg);
        throw new RejectedExecutionException(msg);
    }
}
