package com.miaosha.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private String port;

    @Bean
    public RedissonClient getRedisson(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        //添加主从配置
        // config.useMasterSlaveServers().setMasterAddress("").setPassword("").addSlaveAddress(new String[]{"",""});
        return Redisson.create(config);
    }
}
