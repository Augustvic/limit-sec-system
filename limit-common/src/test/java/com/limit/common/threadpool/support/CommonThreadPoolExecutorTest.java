package com.limit.common.threadpool.support;

import com.limit.common.threadpool.queue.ResizableLinkedBlockingQueue;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class CommonThreadPoolExecutorTest {

    @Test
    public void execute() {
        CommonThreadPoolExecutor executor =  new CommonThreadPoolExecutor(1, 5, 60, TimeUnit.SECONDS,
                new ResizableLinkedBlockingQueue<>(100), new NamedThreadFactory("Test"), new AbortPolicy("Policy"));
        Callable<Integer> task1 = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int result = 0;
                for (int i = 1; i <= 50000; i++) {
                    result += i;
                }
                return result;
            }
        };
        Callable<Integer> task2 = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int result = 0;
                for (int i = 1; i <= 100000; i++) {
                    result += i;
                }
                return result;
            }
        };
        Future future1 = executor.submit(task1);
        Future future2 = executor.submit(task2);
        try {
            System.out.println("The result of task1: " + future1.get());
            System.out.println("The result of task2: " + future2.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Total: " + executor.getTotal());
        System.out.println("Rejected: " + executor.getRejected());
        System.out.println("Submitted: " + executor.getSubmitted());
        System.out.println("TotalElapse: " + executor.getTotalElapse());
        System.out.println("MaxElapse: " + executor.getMaxElapse());
        System.out.println("AverageElpase: " + executor.getAverageElpase());
    }
}