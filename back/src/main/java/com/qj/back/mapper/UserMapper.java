package com.qj.back.mapper;

import com.qj.back.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper{
    @Select("SELECT * FROM user WHERE account = #{account}")
    User findByAccount(String account);

    @Insert("INSERT INTO user(account, password, role) VALUES(#{account}, #{password}, 'user')")
    void insert(User user);


    @Update("UPDATE user SET password = #{password} WHERE account = #{account}")
    void updatePassword(User user);

    @Update("UPDATE user SET avatar_url = #{avatarUrl} WHERE account = #{account}")
    void updateAvatar(User user);

    @Select("SELECT id, account, avatar_url as avatarUrl, role FROM user WHERE id = #{id}")
    User selectById(Integer id);


    @Select("SELECT id, account, avatar_url, role FROM user")  // 注意不返回密码字段
    List<User> selectList();
}
