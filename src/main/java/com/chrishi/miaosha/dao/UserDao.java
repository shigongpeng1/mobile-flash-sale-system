package com.chrishi.miaosha.dao;

import com.chrishi.miaosha.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {

    @Select("select * from user where id=#{id}")
    public User getById(@Param("id") int id);
}
