package com.miaosha.dao;

import com.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GoodsDao {

    @Select("select g.*, mg.stock_count, mg.start_date, mg.end_date, " +
            "mg.miaosha_price from miaosha_goods mg left join goods g on " +
            "mg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*, mg.stock_count, mg.start_date, mg.end_date, " +
            "mg.miaosha_price from miaosha_goods mg left join goods g on " +
            "mg.goods_id = g.id where g.id = #{goodsId}")
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);
}
