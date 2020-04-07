package com.limit.seckill.dispatcher.runnable;

import com.limit.seckill.exchange.message.Request;
import com.limit.seckill.service.SeckillService;

public class DoSeckillRunnable implements Runnable {

    private final Request request;

    private final SeckillService seckillService;

    public DoSeckillRunnable(SeckillService seckillService, Request request) {
        this.seckillService = seckillService;
        this.request = request;
    }

    @Override
    public void run() {
        try {
            seckillService.afterReceiveRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
