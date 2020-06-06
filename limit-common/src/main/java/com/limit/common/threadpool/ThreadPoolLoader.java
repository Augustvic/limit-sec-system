package com.limit.common.threadpool;

import java.util.concurrent.Executor;

public interface ThreadPoolLoader {
    Executor getExecutor(ThreadPoolConfig config);
}
