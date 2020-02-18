package com.miaosha.dao;

import com.miaosha.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {

    @Select("select * from user where id = #{id}")
    public User getById(@Param("id") Integer id);

    @Insert("insert into user(id, name) values (#{id}, #{name})")
    public int insert(User user);
}
