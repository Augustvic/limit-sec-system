package com.limit.seckill.service;

import com.limit.seckill.dao.GoodsDao;
import com.limit.seckill.dao.SeckillGoodsDao;
import com.limit.seckill.entity.Goods;
import com.limit.seckill.entity.SeckillGoods;
import com.limit.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;

    @Autowired
    SeckillGoodsDao seckillGoodsDao;

    // 获取所有商品
    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        Goods goods = goodsDao.getGoodsByGoodsId(goodsId);
        SeckillGoods seckillGoods = seckillGoodsDao.getSeckillGoodById(goodsId);
        return new GoodsVo(goods, seckillGoods);
    }
}
