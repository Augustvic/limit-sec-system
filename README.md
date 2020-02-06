# 秒杀系统实现与优化方案

## 近期思路：

1. 完成缓存预热：定时线程池检查即将开启秒杀的商品

2. 解决缓存击穿的问题

2. 实现 Redis 令牌桶

3. MySQL 优化

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
│  │  │          │      MiaoshaKey.java
│  │  │          │      MiaoshaUserKey.java
│  │  │          │      OrderKey.java
│  │  │          │      RedisConfig.java
│  │  │          │      RedisPoolFactory.java
│  │  │          │      RedisService.java
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
│  │  │          ├─util
│  │  │          │      BaseUtil.java
│  │  │          │      MD5Util.java
│  │  │          │      UUIDUtil.java
│  │  │          │
│  │  │          └─vo
│  │  │                  GoodsDetailVo.java
│  │  │                  GoodsVo.java
│  │  │                  LoginVo.java
│  │  │                  OrderDetailVo.java
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

## 2. 缓存与消息队列

服务先读缓存，如果命中则返回

缓存不命中，再读数据库，同时将读到的数据写入缓存

读多写少的场景（下单成功比例很低）

缓存穿透/缓存击穿/缓存热点/缓存一致性

Redis 令牌机制

### 2.1 消息队列

技术栈：kafka

#### 2.1.1 降流

此操作的目标是将请求尽量拦截在上游，减少对数据库的访问。

短时间大量的秒杀请求不会直接冲击到服务端的处理流程，先堆积在消息队列中，服务按照自己的能力从消息队列中获取消息请求进行处理。

#### 2.1.2 解耦（未涉及）

当一个新的订单创建时，可能会有支付系统需要发起支付流程、风控系统需要审核订单的合法性、客服系统需要给用户发送短信告知用户、分析系统需要更新统计数据……

以上操作都需要实时获取订单数据，所以引入消息队列，下游子系统各自订阅消息，完成各自的流程，互不干扰。

#### 2.1.3 异步

此系统中，决定是否秒杀成功，实际上只有最后一步——写入数据库。异步操作在这里指的是，当秒杀请求进入消息队列，就马上给用户返回“正在处理”。之后由消息队列异步地进行后续的操作，执行完成后才返回秒杀的结果。

### 2.2 缓存

技术栈：redis

#### 2.2.1 缓存预热

线程池定时读取数据库，在商品开始秒杀之前，存入缓存，除此之外，定时清理无效缓存

#### 2.2.2 缓存击穿

在缓存失效（或缓存过期）的情况下，大并发请求缓存中没有但数据库中有的数据。大量请求到达数据库，引起数据库压力瞬间增大。

采用互斥锁解决此问题。缓存失效的时候，先去获取锁，获取到锁的可以去请求数据库，没有得到锁的休眠一段时间再重试。

#### 2.2.3 缓存热点（未实现）

大量业务请求都命中同一份缓存数据。

解决方案：复制多份缓存，将请求分散到多台缓存服务器上。

## 数据库

技术栈：MySQL

### 索引

### 查询优化

## 虚拟机调优

## 其他

### 安全

隐藏秒杀地址

### 超卖

解决超卖：

1. 数据库加唯一索引，防止用户重复购买

2. SQL 加库存数量判断，防止库存变成负数

## 展望

### 分布式

#### 数据库读写分离

#### 缓存雪崩

如果缓存挂掉，所有的请求会压到数据库，如果未提前做容量预估，可能会把数据库压垮。（在缓存恢复之前，数据库可能一直都起不来），导致系统整体不可服务。

提前做容量预估，如果缓存挂掉，数据库仍能扛住，才能执行上述方案。

使用缓存水平切分（推荐使用一致性哈希算法进行切分），一个缓存实例挂掉后，不至于所有的流量都压到数据库上。


