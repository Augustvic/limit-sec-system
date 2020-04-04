package com.limit.seckill.dao;

import com.limit.seckill.entity.Goods;
import com.limit.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GoodsDao {

    @Select("select g.*, mg.stock_count, mg.start_date, mg.end_date, " +
            "mg.seckill_price from seckill_goods mg left join goods g on " +
            "mg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

//    @Select("select g.*, mg.stock_count, mg.start_date, mg.end_date, " +
//            "mg.seckill_price from seckill_goods mg left join goods g on " +
//            "mg.goods_id = g.id where g.id = #{goodsId}")
//    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    @Select("select * from goods where id = #{goodsId}")
    public Goods getGoodsByGoodsId(@Param("goodsId") long goodsId);
}
