package com.limit.common.limiter.slidingwindow;

import com.limit.common.limiter.Limiter;
import com.limit.common.limiter.permitlimiter.PermitLimiter;
import com.limit.redis.key.common.WindowKey;
import com.limit.redis.service.RedisService;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class SlidingWindowLimiter implements Limiter {

    private static final Logger log = LoggerFactory.getLogger(PermitLimiter.class);

    private final RedisService redisService;

    private final String name;

    private final RLock lock;

    private final long limit;

    private final long windowSize;

    private final long intervalNanos;

    // 构造函数
    public SlidingWindowLimiter(SlidingWindowLimiterConfig config) {
        this.redisService = config.getRedisService();
        this.name = config.getName();
        this.lock = config.getLock();
        this.limit = config.getLimit();
        this.windowSize = config.getWindowSize();
        this.intervalNanos = config.getIntervalNanos();
    }

    /**
     * 放入新的（默认）窗口
     * 必须在 lock 内调用
     * @return 返回 Window 实例
     */
    public Window putDefaultWindow() {
        if (!redisService.exists(WindowKey.window, this.name)) {
            Window window = new Window(new LinkedList<>(), intervalNanos, windowSize, limit);
            // 存入缓存，设置有效时间
            redisService.setwe(WindowKey.window, this.name, window, WindowKey.window.expireSeconds());
        }
        return redisService.get(WindowKey.window, this.name, Window.class);
    }

    /**
     * 从缓存获取窗口
     * @return 从缓存获取到的窗口
     */
    private Window getWindow() {
        return redisService.get(WindowKey.window, this.name, Window.class);
    }

    /**
     * 更新
     * @param window 新的窗口
     */
    private void setWindow(Window window) {
        redisService.setwe(WindowKey.window, this.name, window, WindowKey.window.expireSeconds());
    }

    /**
     * 尝试获取锁
     * @return 获取成功返回 true
     */
    private boolean lock() {
        try {
            // 等待 100 秒，获得锁 100 秒后自动解锁
            return lock.tryLock(100, 100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 释放锁
     */
    private void unlock() {
        lock.unlock();
    }

    @Override
    public boolean acquire() {
        return acquire(1);
    }

    public boolean acquire(long tokens) {
        while (true) {
            if (lock()) {
                Window window  = getWindow();
                if (window == null) {
                    window = putDefaultWindow();
                }
                boolean success = window.tryAcquire(tokens);
                try {
                    setWindow(window);
                    return success;
                } finally {
                    unlock();
                }
            }
        }
    }
}
