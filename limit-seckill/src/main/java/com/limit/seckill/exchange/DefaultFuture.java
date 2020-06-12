package com.limit.seckill.exchange;

import com.limit.seckill.exchange.message.Request;
import com.limit.seckill.exchange.message.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFuture {

    public static final Map<Long, DefaultFuture> FUTURES = new ConcurrentHashMap<Long, DefaultFuture>();

    private final long id;
    private final Request request;
    private final long start = System.currentTimeMillis();
    private final long timeout;
    private volatile Response response;

    public DefaultFuture(long id, Request request, long timeout) {
        this.id = id;
        this.request = request;
        this.timeout = timeout;
        FUTURES.put(id, this);
    }

    public static void received(Response response) {
        long id = response.getId();
        DefaultFuture future = FUTURES.get(id);
        if (future != null) {
            future.setResponse(response);
        }
    }

    public static Long get(long id) {
        if (FUTURES.containsKey(id)) {
            DefaultFuture future = FUTURES.get(id);
            if (future.timeout < 0 || System.currentTimeMillis() < (future.start + future.timeout)) {
                Response response = future.getResponse();
                if (response != null) {
                    FUTURES.remove(id);
                    return response.getResult();
                }
                else {
                    return 0L;
                }
            }
            FUTURES.remove(id);
        }
        return -1L;
    }

    public static boolean hasResponse(long id) {
        if (FUTURES.containsKey(id)) {
            DefaultFuture future = FUTURES.get(id);
            Response response = future.getResponse();
            return response != null;
        }
        return false;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return this.response;
    }

    public void cancel() {
        this.response = new Response(this.id, -1);
    }
}
