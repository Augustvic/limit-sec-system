package com.limit.seckill.dao;

import com.limit.seckill.entity.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SeckillGoodsDao {

    @Update("update seckill_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0 ")
    public int reduceStock(SeckillGoods g);

    @Select("select * from seckill_goods where start_date >= #{startDate} and start_date <= #{endDate}")
    public List<SeckillGoods> listSeckillGoodsLatest(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select("select * from seckill_goods where id = #{goodsId}")
    public SeckillGoods getSeckillGoodById(@Param("goodsId") Long goodsId);
}
