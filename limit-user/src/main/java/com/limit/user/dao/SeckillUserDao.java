package com.limit.user.dao;

import com.limit.user.entity.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Created by August on 2019/6/15 15:35
 **/

@Mapper
public interface SeckillUserDao {

    @Select("select * from seckill_user where id = #{id}")
    public SeckillUser getById(@Param("id") long id);

    @Update("update seckill_user set password = #{password} where id = #{id}")
    public void update(SeckillUser toBeUpdate);
}
