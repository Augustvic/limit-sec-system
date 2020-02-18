# 秒杀系统实现与优化方案

## 0. 近期思路：

MySQL 优化

虚拟机调优

## 1. 概述

技术栈：MySQL / Redis / Kafka

系统的代码结构如下所示：

```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─com
│  │  │      └─miaosha
│  │  │          │  MainApplication.java
│  │  │          │  
│  │  │          ├─access
│  │  │          │      AccessInterceptor.java 
│  │  │          │      AccessLimit.java
│  │  │          │      UserContext,java
│  │  │          │  
│  │  │          ├─config
│  │  │          │     BloomFilterConfig.java
│  │  │          │     RateLimiterConfig.java
│  │  │          │     RedissonConfig.java
│  │  │          │     ThreadPoolConfig.java  
│  │  │          │     UserArgumentResolver.java
│  │  │          │     WebConfig.java
│  │  │          │  
│  │  │          ├─controller
│  │  │          │      GoodsController.java
│  │  │          │      LoginController.java
│  │  │          │      MiaoshaController.java
│  │  │          │      OrderController.java
│  │  │          │      SampleController.java
│  │  │          │      UserController.java
│  │  │          │      
│  │  │          ├─dao
│  │  │          │      GoodsDao.java
│  │  │          │      MiaoshaGoodsDao.java
│  │  │          │      MiaoshaUserDao.java
│  │  │          │      OrderDao.java
│  │  │          │      UserDao.java
│  │  │          │     
│  │  │          ├─entity
│  │  │          │      Goods.java
│  │  │          │      MiaoshaGoods.java
│  │  │          │      MiaoshaOrder.java
│  │  │          │      MiaoshaUser.java
│  │  │          │      OrderInfo.java
│  │  │          │      User.java
│  │  │          │
│  │  │          ├─exception
│  │  │          │      GlobalException.java
│  │  │          │      GlobalExceptionHandler.java
│  │  │          │
│  │  │          ├─kafka
│  │  │          │      MiaoshaMessage.java
│  │  │          │      MQConfig.java
│  │  │          │      MQReceiver.java
│  │  │          │      MQSend.java
│  │  │          │
│  │  │          ├─redis
│  │  │          │      AccessKey.java
│  │  │          │      BasePrefix.java
│  │  │          │      GoodsKey.java
│  │  │          │      KeyPrefix.java
│  │  │          │      LockKey.java
│  │  │          │      MiaoshaKey.java
│  │  │          │      MiaoshaUserKey.java
│  │  │          │      OrderKey.java
│  │  │          │      RedisConfig.java
│  │  │          │      RedisPoolFactory.java
│  │  │          │      RedisService.java
│  │  │          │      RedissonService.java
│  │  │          │      UserKey.java
│  │  │          │
│  │  │          ├─result
│  │  │          │      CodeMsg.java
│  │  │          │      Result.java
│  │  │          │
│  │  │          ├─service
│  │  │          │      GoodsService.java
│  │  │          │      MiaoshaService.java
│  │  │          │      MiaoshaUserService.java
│  │  │          │      OrderService.java
│  │  │          │      UserService.java
│  │  │          │
│  │  │          ├─vo
│  │  │          │      GoodsDetailVo.java
│  │  │          │      GoodsVo.java
│  │  │          │      LoginVo.java
│  │  │          │      OrderDetailVo.java
│  │  │          │
│  │  │          └─util
│  │  │              │  BaseUtil.java
│  │  │              │  MD5Util.java
│  │  │              │  UUIDUtil.java
│  │  │              │
│  │  │              └─concurrent
│  │  │                       BloomFilter.java
│  │  │                       PermitBucket.java
│  │  │                       RateLimiter.java
│  │  │        
│  │  │                      
│  │  └─resources
│  │      │  application.yml
│  │      │      
│  │      ├─static
│  │      └─templates
└─

```

## 2. 秒杀功能优化

秒杀核心功能的总体流程如下图所示：

<img src="https://github.com/Augustvic/MiaoShaoSystem/blob/master/images/main.png" width=70% />

### 2.1 消息队列

#### 2.1.1 降流

目标是将请求尽量拦截在上游，减少对数据库的访问。

短时间大量的秒杀请求不会直接冲击到服务端的处理流程，先堆积在消息队列中，服务按照自己的能力从消息队列中获取消息请求进行处理。

#### 2.1.2 解耦（未完成）

当一个新的订单创建时，可能会有支付系统需要发起支付流程、风控系统需要审核订单的合法性、客服系统需要给用户发送短信告知用户、分析系统需要更新统计数据……

以上操作都需要实时获取订单数据，所以引入消息队列，下游子系统各自订阅消息，完成各自的流程，互不干扰。

#### 2.1.3 异步

此系统中，决定是否秒杀成功，实际上只有最后一步——写入数据库。

当秒杀请求进入消息队列，就马上给用户返回“正在处理”。之后由消息队列异步地进行后续的操作，执行完成后才返回秒杀的结果。

前端页面不断调用接口查询是否已经

### 2.2 缓存

技术栈：Redis

#### 2.2.1 缓存预热

在秒杀开始之前，提前将秒杀商品库存加载到缓存中。

此项目中使用定时线程池每 20 分钟读取一次数据库，获取即将参加秒杀的商品库存，存入缓存中。

#### 2.2.2 缓存击穿

在缓存失效（或缓存过期）的情况下，大并发请求缓存中没有但数据库中有的数据。大量请求到达数据库，引起数据库压力瞬间增大。

使用互斥锁限制访问数据库请求。缓存失效的时候，线程先去获取锁，获取到锁的线程请求数据库数据，没有得到锁的继续不断重试并检查缓存中是否有了该数据。读取数据库的线程将数据加入到缓存后释放锁。

项目中使用 Redis 实现的互斥锁来避免缓存击穿的功能点主要包括：

* “预减库存”之前判断库存是否已加载到缓存中，如果未加载，线程请求互斥锁。请求成功的线程访问数据库，将秒杀商品库存加载到缓存中，其他请求在此过程中自旋等待。此模块共设置互斥锁 100 个，对同一商品而言只会有一个线程访问数据库获得库存。

#### 2.2.3 缓存热点（未完成）

大量业务请求都命中同一份缓存数据。

解决方案：复制多份缓存，将请求分散到多台缓存服务器上。

#### 2.2.4 令牌桶

使用令牌桶控制数据库访问的速率。分布式令牌桶仿照 Guava 工具包的 RateLimiter 实现，使用 Redisson 中的互斥锁保障 Redis 写入安全。

此项目中使用令牌桶控制应用层访问数据库的频率。读取数据库之前需要从读令牌桶中获取读令牌，写入数据库之前需要从写令牌桶中获取写令牌，获取成功才可进行相应的操作，否则重新进入消息队列中等待下一次获取令牌的机会。

令牌桶的详细设计请参考 “[基于 Redis 的分布式令牌桶设计与实现](https://blog.csdn.net/Victorgcx/article/details/104248819)”。

#### 2.2.5 订单缓存

使用了消息队列的下单操作是异步的，前端将不断轮询秒杀结果。将订单写入数据库之后，需要再将订单存入缓存中，以便轮询时能及时查询到订单状态和订单信息，并将其反馈给用户。

此模块使用 Redis 的 hash 数据结构保存用户的秒杀订单，用户的 ID 作为键，单个商品的 id 作为 hash 结构的 key，订单详情作为 hash 结构的 value。

### 2.3 数据库

数据库表设计详见 "[miaoshadb](https://github.com/Augustvic/MiaoShaoSystem/blob/master/images/db.md)"

#### 2.3.1 索引与查询优化

项目相关 sql 语句的索引创建及查询优化详见博客 [索引与查询性能优化](https://blog.csdn.net/Victorgcx/article/details/102595580)。

### 2.4 其他

#### 2.4.1 避免超卖

1. 减库存的 sql 语句中判断库存大于 0 时才执行减库存操作。

2. 数据库秒杀订单表的（用户，商品）联合字段加唯一索引。

3. 执行 “减库存 下订单 写入秒杀订单” 事务前，设置 Redis 互斥锁，将并行请求串行化，只有获取到互斥锁的线程才能开启事务。此方法属于悲观锁操作。

4. 为秒杀操作添加版本号。在减库存之前，先检查版本号是否被改变，如果未被改变才能执行。此方法属于乐观锁操作。

此秒杀模块使用前三种方法避免超卖。

#### 2.4.2 访问限制及黑名单

用户请求秒杀地址前过滤访问请求，将用户访问次数存入缓存中。

此处设置当单个用户 5 秒内请求 5 次，即将该用户加入黑名单。黑名单由布隆过滤器实现。在固定时间内，直接拒绝黑名单用户的访问请求，并提示用户“已被加入黑名单”。

#### 2.4.3 验证码

用户点击购买按钮之前需要输入图片验证码，分散用户请求。将验证码结果存于缓存中，验证通过开始秒杀流程，验证失败则继续验证。

#### 2.4.4 秒杀地址隐藏

为每一次秒杀生成随机的秒杀地址，将地址存入缓存中，执行秒杀核心流程之前，验证地址是否正确，地址如果正确则继续后面的流程。

## 3. 用户模块优化

### 3.1 用户会话

使用 Redis 中的 hash 数据结构缓存用户的基本信息和会话信息。读写单个用户信息不会出现高并发环境，所以此模块对 redis 的读写不需要加互斥锁。

以最后一次登录时间开始算起，五分钟之后，会话将会过期，用户需要重新登陆。

> Session是在服务端保存的一个数据结构，用来跟踪用户的状态，这个数据可以保存在集群、数据库、文件中；Cookie是客户端保存用户信息的一种机制，用来记录用户的一些信息，也是实现 Session 的一种方式。

### 3.2 用户信息修改

#### 3.2.1 缓存淘汰还是修改？

朴素类型的数据，直接 set 修改后的值即可；对于对象类型或者文本类型，修改缓存 value 的成本较高，需要反序列化和序列化，直接淘汰缓存更好。

在少数场景下选择修改缓存效率较高，但是修改缓存可能会出现以下并发问题：

（1）线程 1 先操作数据库，线程 2 后操作数据库

（2）线程 2 先 set 了缓存，线程 1 后 set 了缓存

将会导致，数据库与缓存之间的数据不一致。所以通常情况下选择淘汰缓存，因为最多额外增加一次 cache miss，成本忽略不计。

但此处客户端对于单个用户信息的访问不存在高并发读取的情况，所以选择**修改缓存**即可。

#### 3.2.2 缓存与数据库一致性

通常情况下建议先操作数据库再操作缓存。

此处客户端对于单个用户信息的访问不存在高并发读取的情况，可先修改缓存，保证客户端即刻就能读取到最新信息，然后再写入数据库。

## 4. 虚拟机调优




## 5. 展望

### 5.1 分布式（未完成）

#### 5.1.1 数据库读写分离

#### 5.1.2 缓存雪崩

如果缓存挂掉，所有的请求会压到数据库，如果未提前做容量预估，可能会把数据库压垮。（在缓存恢复之前，数据库可能一直都起不来），导致系统整体不可服务。

提前做容量预估，如果缓存挂掉，数据库仍能扛住，才能执行上述方案。

使用缓存水平切分（推荐使用一致性哈希算法进行切分），一个缓存实例挂掉后，不至于所有的流量都压到数据库上。


## 6. 参考

* [（讨论）缓存同步、如何保证缓存一致性、缓存误用](https://www.jianshu.com/p/c8d5df3338aa)
