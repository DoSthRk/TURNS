package com.qj.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qj.back.entity.Advice;

import org.apache.ibatis.annotations.*;
import java.util.List;

public interface AdviceMapper extends BaseMapper<Advice> {
    @Insert("INSERT INTO advice(text, user_id, create_time) " +
            "VALUES(#{text}, #{userId}, #{createTime})")
    int insert(Advice advice);

    @Select("SELECT " +
            "a.id, a.text, a.user_id, a.create_time, " +
            "u.id as 'user.id', " +
            "u.account as 'user.account', " +
            "u.avatar_url as 'user.avatarUrl', " +  // 确保获取头像URL
            "u.role as 'user.role' " +
            "FROM advice a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "ORDER BY a.create_time DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "text", column = "text"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "user", column = "user_id", one = @One(select = "com.qj.back.mapper.UserMapper.selectById"))
    })
    List<Advice> selectList();

    @Delete("DELETE FROM advice WHERE id = #{id}")
    int deleteById(Integer id);

    @Select("SELECT a.*, u.account, u.avatar " +
            "FROM advice a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE u.account = #{account} " +
            "ORDER BY a.id DESC")  // 暂时使用 id 排序
    List<Advice> selectByAccount(String account);


}
