# 数据库设计

#### 商品表

| 字段名 | 数据类型 | 注释 |
| - | - | - |
| goods_detail | longtext | 商品的详细介绍 |
| goods_img | varchar(64) | 商品的图片 |
| goods_name | varchar(16) | 商品名称 |
| goods_price | decimal(10,2) | 商品单价 |
| goods_stock | int(11) | 商品库存, -1 表示没有限制 |
| goods_title | varchar(255) | 商品 |
| id | bigint(20) | 商品ID |

#### 秒杀商品表

| 字段名 | 数据类型 | 注释 |
| - | - | - |
| end_date | datetime | 秒杀结東时间 |
| goods_id | bigint(20) | 商品id |
| id | bigint(20) | 秒杀的商品表 |
| seckill_price | decimal(10,2) | 秒杀价 |
| start_date | datetime | 秒杀开始时间 |
| stock_count | int(11) | 库存数量 |

#### 秒杀订单表

| 字段名 | 数据类型 | 注释 |
| - | - | - |
| goods_id | bigint(20) | 商品ID |
| id | bigint(20) |  |
| order_id | bigint(20) | 订单ID |
| user_id | bigint(20) | 用户ID |

#### 秒杀用户表

| 字段名 | 数据类型 | 注释 |
| - | - | - |
| head | varchar(128) | 头像，云存储的ID |
| id | bigint(20) | 用户ID, 手机号码 |
| last_login_date | datetime | 上次登录时间 |
| login_count | int(11) | 登录次数 |
| nickname | varchar(255) |  |
| password | varchar(32) | MD5(MD5(pass明文+固定salt) + salt) |
| register_date | datetime | 注册时间 |
| salt | varchar(10) |  |	


#### 订单表

| 字段名 | 数据类型 | 注释 |
| - | - | - |
| create_date | datetime | 订单的创建时间 |
| delivery_addr_id | bigint(20) | 收货地址ID |
| goods_count | int(11) | 商品数量 |
| goods_id | bigint(20) | 商品ID |
| goods_name | varchar(16) | 冗余过来的商品名称 |
| goods_price | decimal(10,2) | 商品单价 |
| id | bigint(20) |  |
| order_channel | tinyint(4) | 1pc, 2android, 3ios |
| pay_date | datetime | 支付时间 |
| status | tinyint(4) | 订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退款，5已完成 |
| user_id | bigint(20) | 用户ID |
