package com.limit.common.limiter.slidingwindow;

public class Node {
    private long startTime;
    private long endTime;
    private long count;

    public Node(long startTime, long endTime, long count) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.count = count;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void addCount(long num) {
        this.count += num;
    }
}
