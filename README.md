# 秒杀系统实现与优化方案

## 0. 近期思路：

检查缓存中是否已经有了订单，将某个商品的订单列表以 redis 链表形式保存

手写 LRU 算法

MySQL 优化

虚拟机调优

## 1. 概述

基础框架：SpringBoot 

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

秒杀核心功能的总体流程如下图所示：

<img src="https://github.com/Augustvic/MiaoShaoSystem/blob/master/images/main.png" width=70% />

## 2. 秒杀模块优化

服务先读缓存，如果命中则返回

缓存不命中，再读数据库，同时将读到的数据写入缓存

读多写少的场景（下单成功比例很低）

缓存穿透/缓存击穿/缓存热点/缓存一致性

### 2.1 消息队列

技术栈：kafka

#### 2.1.1 降流

目标是将请求尽量拦截在上游，减少对数据库的访问。

短时间大量的秒杀请求不会直接冲击到服务端的处理流程，先堆积在消息队列中，服务按照自己的能力从消息队列中获取消息请求进行处理。

#### 2.1.2 解耦（未涉及）

当一个新的订单创建时，可能会有支付系统需要发起支付流程、风控系统需要审核订单的合法性、客服系统需要给用户发送短信告知用户、分析系统需要更新统计数据……

以上操作都需要实时获取订单数据，所以引入消息队列，下游子系统各自订阅消息，完成各自的流程，互不干扰。

#### 2.1.3 异步

此系统中，决定是否秒杀成功，实际上只有最后一步——写入数据库。

当秒杀请求进入消息队列，就马上给用户返回“正在处理”。之后由消息队列异步地进行后续的操作，执行完成后才返回秒杀的结果。

### 2.2 缓存

技术栈：redis

#### 2.2.1 缓存预热

在秒杀开始之前，提前将秒杀商品库存加载到缓存中。

此项目中使用定时线程池每 20 分钟读取一次数据库，获取即将参加秒杀的商品库存，存入缓存中。

#### 2.2.2 缓存击穿

在缓存失效（或缓存过期）的情况下，大并发请求缓存中没有但数据库中有的数据。大量请求到达数据库，引起数据库压力瞬间增大。

采用互斥锁解决此问题。缓存失效的时候，线程先去获取锁，获取到锁的线程请求数据库数据，没有得到锁的继续不断重试并检查缓存中是否有了该数据。读取数据库的线程将数据加入到缓存后释放锁。

此项目中使用 redis 实现的互斥锁来避免缓存击穿的功能点主要包括：

* “预减库存”之前判断库存是否已加载到缓存中，如果未加载，线程请求互斥锁。请求成功的线程访问数据库，将秒杀商品库存加载到缓存中，其他请求在此过程中自旋等待。

#### 2.2.3 缓存热点（未实现）

大量业务请求都命中同一份缓存数据。

解决方案：复制多份缓存，将请求分散到多台缓存服务器上。

#### 2.2.4 令牌桶




### 2.3 数据库

技术栈：MySQL

#### 2.3.1 索引

#### 2.3.2 查询优化



## 3. 用户模块优化

技术栈：Redis

### 3.1 用户会话

使用 Redis 中的 hash 数据结构缓存用户的基本信息和会话信息。

以最后一次登录时间开始算起，五分钟之后，会话将会过期，用户需要重新登陆。

> Session是在服务端保存的一个数据结构，用来跟踪用户的状态，这个数据可以保存在集群、数据库、文件中；Cookie是客户端保存用户信息的一种机制，用来记录用户的一些信息，也是实现 Session 的一种方式。

### 3.2 信息修改

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


## 5. 其他

### 5.1 安全

隐藏秒杀地址

### 5.2 超卖

解决超卖：

1. 数据库加唯一索引，防止用户重复购买

2. SQL 加库存数量判断，防止库存变成负数


## 6. 展望

### 分布式

#### 数据库读写分离

#### 缓存雪崩

如果缓存挂掉，所有的请求会压到数据库，如果未提前做容量预估，可能会把数据库压垮。（在缓存恢复之前，数据库可能一直都起不来），导致系统整体不可服务。

提前做容量预估，如果缓存挂掉，数据库仍能扛住，才能执行上述方案。

使用缓存水平切分（推荐使用一致性哈希算法进行切分），一个缓存实例挂掉后，不至于所有的流量都压到数据库上。

## 7. 参考

* [（讨论）缓存同步、如何保证缓存一致性、缓存误用](https://www.jianshu.com/p/c8d5df3338aa)
