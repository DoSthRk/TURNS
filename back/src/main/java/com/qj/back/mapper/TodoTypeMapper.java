package com.qj.back.mapper;

import com.qj.back.entity.TodoType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TodoTypeMapper {
    @Select("SELECT * FROM todo_type WHERE shift_type_id = #{shiftTypeId}")
    List<TodoType> selectByShiftTypeId(Integer shiftTypeId);

    @Select("SELECT * FROM todo_type WHERE id = #{id}")
    TodoType selectById(Integer id);
}
