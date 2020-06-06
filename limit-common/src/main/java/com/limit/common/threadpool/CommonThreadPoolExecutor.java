package com.limit.common.threadpool;

import com.limit.common.threadpool.queue.ResizableLinkedBlockingQueue;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class CommonThreadPoolExecutor extends ThreadPoolExecutor {

    // task statistics information
    private final static Map<Runnable, Status> RUNNABLE_STATISTICS = new ConcurrentHashMap<>();
    // number of all tasks
    private final AtomicLong total = new AtomicLong();
    // number of rejected tasks
    private final AtomicLong rejected = new AtomicLong();
    // current submitted task count in this queue
    private final AtomicLong submitted = new AtomicLong();
    // elapse of all tasks
    private final AtomicLong totalElapse = new AtomicLong();
    // max elapse of all tasks
    private final AtomicLong maxElapse = new AtomicLong();
    // average elapse of all tasks
    private final AtomicLong averageElapse = new AtomicLong();

    public CommonThreadPoolExecutor(int corePoolSize,
                                   int maximumPoolSize,
                                   long keepAliveTime,
                                   TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                   ThreadFactory threadFactory,
                                   RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    /**
     * @return current tasks which are executed
     */
    public long getSubmitted() {
        return submitted.get();
    }

    public long getTotal() {
        return total.get();
    }

    public long getRejected() {
        return rejected.get();
    }

    public long getTotalElapse() {
        return totalElapse.get();
    }

    public long getMaxElapse() {
        return maxElapse.get();
    }

    public long getAverageElpase() {
        return averageElapse.get();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        submitted.decrementAndGet();
        total.incrementAndGet();
        Status status = RUNNABLE_STATISTICS.get(r);
        status.endTime = System.currentTimeMillis();
        long elapse = status.endTime - status.startTime;
        totalElapse.addAndGet(elapse);
        if (elapse > maxElapse.get())
            maxElapse.set(elapse);
        averageElapse.set(totalElapse.get() / total.get());
        RUNNABLE_STATISTICS.remove(r);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        Status status = new Status(r, System.currentTimeMillis());
        RUNNABLE_STATISTICS.putIfAbsent(r, status);
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        // 不在 beforeExecute 中执行，因为统计的是提交任务数而不是执行任务数
        submitted.incrementAndGet();
        try {
            super.execute(command);
        } catch (RejectedExecutionException e) {
            submitted.decrementAndGet();
            rejected.incrementAndGet();
            throw new RejectedExecutionException(e);
        } catch (Throwable t) {
            // 无论如何，计数都要减 1
            submitted.decrementAndGet();
            throw t;
        }
    }

    /**
     * set capacity of work queue
     * @param capacity of work queue
     */
    public void setWorkQueueCapacity(int capacity) {
        ResizableLinkedBlockingQueue<Runnable> queue = (ResizableLinkedBlockingQueue<Runnable>) getQueue();
        queue.setCapacity(capacity);
    }

    // the status of runnable
    static class Status {
        Runnable task;
        long startTime;
        long endTime;

        public Status(Runnable task, long startTime, long endTime) {
            this.task = task;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Status(Runnable task, long startTime) {
            this.task = task;
            this.startTime = startTime;
        }
    }
}
