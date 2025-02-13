package com.qj.back.mapper;

import com.qj.back.entity.UserTodo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserTodoMapper {
    @Select("SELECT ut.*, tt.content as 'todoType.content', tt.shift_type_id as 'todoType.shiftTypeId' " +
            "FROM user_todo ut " +
            "LEFT JOIN todo_type tt ON ut.todo_type_id = tt.id " +
            "WHERE ut.user_id = #{userId} AND ut.schedule_id = #{scheduleId}")
    List<UserTodo> selectByUserAndSchedule(@Param("userId") Integer userId,
                                           @Param("scheduleId") Integer scheduleId);

    @Insert("INSERT INTO user_todo(user_id, todo_type_id, schedule_id, is_completed) " +
            "VALUES(#{userId}, #{todoTypeId}, #{scheduleId}, #{isCompleted})")
    int insert(UserTodo userTodo);

    @Update("UPDATE user_todo SET is_completed = #{isCompleted}, " +
            "complete_time = #{completeTime} WHERE id = #{id}")
    int updateStatus(UserTodo userTodo);

    @Delete("DELETE FROM user_todo WHERE schedule_id = #{scheduleId}")
    int deleteByScheduleId(@Param("scheduleId") Integer scheduleId);
}
