package com.limit.common.threadpool;

import com.limit.common.threadpool.queue.ResizableLinkedBlockingQueue;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ResizableLinkedBlockingQueueTest {

    @Test
    public void setCapacityTest() {
        int oldCapacity = 20;
        int newCapacity = 10;
        final AtomicInteger num = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(50);
        final ResizableLinkedBlockingQueue<Integer> queue = new ResizableLinkedBlockingQueue<>(oldCapacity);
        Thread producer1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        queue.put(num.getAndIncrement());
                        latch.countDown();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Thread producer2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        queue.put(num.getAndIncrement());
                        latch.countDown();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Thread consumer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("起始容量：" + queue.capacity());
                    while (!queue.isEmpty()) {
                        System.out.println("队列：" + queue.toString());
                        System.out.println("消费：" + queue.take());
                        Thread.sleep(100);
                    }
                    System.out.println("终止容量：" + queue.capacity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        producer1.start();
        producer2.start();
        consumer.start();
        try {
            latch.await();
            queue.setCapacity(newCapacity);
            Thread.sleep(1000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}