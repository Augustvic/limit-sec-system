package com.limit.common;

public class Constants {

    // ThreadPool
    public static final String THREAD_POOL_NAME = "Limit";
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    public static final int DEFAULT_CORE_THREADS =  CPUS + 1;
    public static final int DEFAULT_MAX_THREADS = Integer.MAX_VALUE;
    public static final int DEFAULT_BLOCKING_QUEUE = 1000;
    public static final int DEFAULT_ALIVE_TIME = 60 * 1000;
    public static final String COMMON_THREAD_POOL = "Common";
    public static final String FIXED_THREAD_POOL = "Fixed";
    public static final String CACHED_THREAD_POOL = "Cached";
    public static final String SCHEDULED_THREAD_POOL = "Scheduled";

    // Distributed Lock
    public static final int N_LOCKS = 1000;

    // Cache
    public static final int CACHE_MAX_CAPACITY = 10000;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    public static final String LRU_CACHE = "LRU";
    public static final String LFU_CACHE = "LFU";
    public static final String LRU2_CACHE = "LRU2";

    // PermitLimiter
    public static final long PERMITS_PER_SECOND = 100L;
    public static final long MAX_PERMITS = 10000L;

    // BloomFilter
    public static final long EXPECTED_INSERTIONS = 1000L;
    public static final double FPP = 0.01d;

    // LeakyLimiter
    public static final long LEAK_PER_SECOND = 1L;

    // SlidingWindowLimiter
    public static final long LIMIT_COUNT = 1000L;
    public static final long WINDOW_SIZE = 10L;
}
