package com.limit.seckill.dispatcher.support;

import com.limit.common.extension.ExtensionLoader;
import com.limit.common.threadpool.ThreadPool;
import com.limit.common.threadpool.ThreadPoolConfig;
import com.limit.seckill.dispatcher.Dispatcher;
import com.limit.seckill.dispatcher.runnable.DoSeckillRunnable;
import com.limit.seckill.exchange.message.Request;
import com.limit.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class DoSeckillDispatcher implements Dispatcher {

    @Autowired
    SeckillService seckillService;

    private final ExecutorService executor;

    public DoSeckillDispatcher() {
        ThreadPoolConfig config = new ThreadPoolConfig("DoSeckill");
        executor = (ExecutorService) ExtensionLoader.getExtensionLoader(ThreadPool.class).getExtension("common").getExecutor(config);;
    }

    @Override
    public void received(Object msg) {
        if (msg instanceof Request) {
            executor.execute(new DoSeckillRunnable(seckillService, (Request)msg));
        }
    }
}
