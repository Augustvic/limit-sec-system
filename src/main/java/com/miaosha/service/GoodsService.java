package com.miaosha.service;

import com.miaosha.dao.GoodsDao;
import com.miaosha.dao.MiaoshaGoodsDao;
import com.miaosha.entity.Goods;
import com.miaosha.entity.MiaoshaGoods;
import com.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;

    @Autowired
    MiaoshaGoodsDao miaoshaGoodsDao;

    // 获取所有商品
    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        Goods goods = goodsDao.getGoodsByGoodsId(goodsId);
        MiaoshaGoods miaoshaGoods = miaoshaGoodsDao.getMiaoshaGoodById(goodsId);
        return new GoodsVo(goods, miaoshaGoods);
    }
}
