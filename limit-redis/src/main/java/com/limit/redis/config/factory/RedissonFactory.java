package com.limit.redis.config.factory;

import com.limit.redis.config.RedissonConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class RedissonFactory {

    @Autowired
    RedissonConfig redissonConfig;

    @Bean
    public RedissonClient getRedisson(){
        Config config = new Config();
        String host = redissonConfig.getHost();
        String port = redissonConfig.getPort();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        //添加主从配置
        // config.useMasterSlaveServers().setMasterAddress("").setPassword("").addSlaveAddress(new String[]{"",""});
        return Redisson.create(config);
    }
}
