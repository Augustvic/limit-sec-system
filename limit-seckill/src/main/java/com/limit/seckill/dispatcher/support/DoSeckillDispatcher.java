package com.limit.seckill.dispatcher.support;

import com.limit.common.threadpool.ThreadPoolFactory;
import com.limit.common.threadpool.support.CommonThreadPoolExecutor;
import com.limit.common.threadpool.support.ThreadPoolConfig;
import com.limit.seckill.dispatcher.Dispatcher;
import com.limit.seckill.dispatcher.runnable.DoSeckillRunnable;
import com.limit.seckill.exchange.message.Request;
import com.limit.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class DoSeckillDispatcher implements Dispatcher {

    @Autowired
    SeckillService seckillService;

    @Autowired
    ThreadPoolFactory threadPoolFactory;

    private CommonThreadPoolExecutor executor;

    @PostConstruct
    public void init() {
        ThreadPoolConfig config = new ThreadPoolConfig("DoSeckill");
        config.setCorePoolSize(10);
        config.setMaximumPoolSize(20);
        executor = (CommonThreadPoolExecutor) threadPoolFactory.getCommonThreadPool(config);
    }

    @Override
    public void received(Object msg) {
        if (msg instanceof Request) {
//            try {
//                seckillService.afterReceiveRequest((Request)msg);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            executor.execute(new DoSeckillRunnable(seckillService, (Request)msg));
        }
    }
}
