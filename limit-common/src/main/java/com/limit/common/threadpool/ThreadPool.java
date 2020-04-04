package com.limit.common.threadpool;

import com.limit.common.extension.SPI;

import java.util.concurrent.Executor;

@SPI("common")
public interface ThreadPool {
    Executor getExecutor(ThreadPoolConfig config);
}
