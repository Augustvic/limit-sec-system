package com.limit.seckill.exchange.message;

public class Response {

    private final long id;
    // 小于 0 表示失败
    // 等于 0 需要继续等待
    // 大于 0 表示成功
    private long result;

    public Response(long id) {
        this.id = id;
    }

    public Response(long id, long result) {
        this.id = id;
        this.result = result;
    }

    public long getId() {
        return id;
    }

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }
}
