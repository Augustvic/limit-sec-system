package com.miaosha.dao;

import com.miaosha.entity.MiaoshaGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MiaoshaGoodsDao {

    @Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0 ")
    public int reduceStock(MiaoshaGoods g);

    @Select("select * from miaosha_goods where start_date >= #{startDate} and start_date <= #{endDate}")
    public List<MiaoshaGoods> listMiaoshaGoodsLatest(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
