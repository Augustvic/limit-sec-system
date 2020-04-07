package com.limit.seckill.service;

import com.limit.seckill.dao.GoodsDao;
import com.limit.seckill.dao.SeckillGoodsDao;
import com.limit.seckill.entity.Goods;
import com.limit.seckill.entity.SeckillGoods;
import com.limit.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface GoodsService {

    // 获取所有商品
    List<GoodsVo> listGoodsVo();

    GoodsVo getGoodsVoByGoodsId(long goodsId);
}
