package com.limit.common.threadpool;

import com.limit.common.threadpool.support.ThreadPoolConfig;

import java.util.concurrent.Executor;

public interface ThreadPoolLoader {
    Executor getExecutor(ThreadPoolConfig config);
}
