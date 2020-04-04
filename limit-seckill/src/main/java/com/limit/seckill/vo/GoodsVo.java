package com.limit.seckill.vo;

import com.limit.seckill.entity.Goods;
import com.limit.seckill.entity.SeckillGoods;

import java.util.Date;

public class GoodsVo extends Goods {
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
    private Double seckillPrice;

    public Double getSeckillPrice() {
        return seckillPrice;
    }

    public void setSeckillPrice(Double seckillPrice) {
        this.seckillPrice = seckillPrice;
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

    public GoodsVo(Goods goods, SeckillGoods seckillGoods) {
        this.setId(goods.getId());
        this.setGoodsName(goods.getGoodsName());
        this.setGoodsTitle(goods.getGoodsTitle());
        this.setGoodsImg(goods.getGoodsImg());
        this.setGoodsPrice(goods.getGoodsPrice());
        this.setGoodsStock(goods.getGoodsStock());
        this.setStockCount(seckillGoods.getStockCount());
        this.setStartDate(seckillGoods.getStartDate());
        this.setEndDate(seckillGoods.getEndDate());
        this.setSeckillPrice(seckillGoods.getSeckillPrice());
    }

    public GoodsVo() {}
}
