package com.qj.back.mapper;

import com.qj.back.entity.Schedule;
import org.apache.ibatis.annotations.*;


import java.util.List;

@Mapper
public interface ScheduleMapper {

    @Insert("INSERT INTO schedule(date, shift_type_id, user_id, created_by, create_time) " +
            "VALUES(#{date}, #{shiftTypeId}, #{userId}, #{createdBy}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 添加这行以获取自增ID
    int insert(Schedule schedule);

    @Select("SELECT s.*, " +
            "st.name as 'shiftType.name', " +
            "st.color as 'shiftType.color', " +
            "u.account as 'user.account', " +
            "u.avatar_url as 'user.avatarUrl' " +
            "FROM schedule s " +
            "LEFT JOIN shift_type st ON s.shift_type_id = st.id " +
            "LEFT JOIN user u ON s.user_id = u.id " +
            "ORDER BY s.date DESC")
    List<Schedule> selectList();


    @Delete("DELETE FROM schedule WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Select("SELECT COUNT(*) FROM schedule " +
            "WHERE date = #{date} AND user_id = #{userId}")
    int countByDateAndUser(@Param("date") String date, @Param("userId") Integer userId);

    @Select("SELECT * FROM schedule WHERE user_id = #{userId} AND date = #{date}")
    Schedule selectTodaySchedule(@Param("userId") Integer userId, @Param("date") String date);

    @Select("SELECT s.*, " +
            "st.name as 'shiftType.name', " +
            "st.color as 'shiftType.color', " +
            "u.account as 'user.account', " +
            "u.avatar_url as 'user.avatarUrl' " +
            "FROM schedule s " +
            "LEFT JOIN shift_type st ON s.shift_type_id = st.id " +
            "LEFT JOIN user u ON s.user_id = u.id " +
            "WHERE s.id = #{id}")
    Schedule selectById(@Param("id") Integer id);


}

