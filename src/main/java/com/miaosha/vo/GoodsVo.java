package com.miaosha.vo;

import com.miaosha.entity.Goods;
import com.miaosha.entity.MiaoshaGoods;

import java.util.Date;

public class GoodsVo extends Goods {
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
    private Double miaoshaPrice;

    public Double getMiaoshaPrice() {
        return miaoshaPrice;
    }

    public void setMiaoshaPrice(Double miaoshaPrice) {
        this.miaoshaPrice = miaoshaPrice;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public GoodsVo(Goods goods, MiaoshaGoods miaoshaGoods) {
        this.setId(goods.getId());
        this.setGoodsName(goods.getGoodsName());
        this.setGoodsTitle(goods.getGoodsTitle());
        this.setGoodsImg(goods.getGoodsImg());
        this.setGoodsPrice(goods.getGoodsPrice());
        this.setGoodsStock(goods.getGoodsStock());
        this.setStockCount(miaoshaGoods.getStockCount());
        this.setStartDate(miaoshaGoods.getStartDate());
        this.setEndDate(miaoshaGoods.getEndDate());
        this.setMiaoshaPrice(miaoshaGoods.getMiaoshaPrice());
    }
}
