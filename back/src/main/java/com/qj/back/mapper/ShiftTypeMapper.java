package com.qj.back.mapper;

import com.qj.back.entity.ShiftType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShiftTypeMapper {

    @Select("SELECT * FROM shift_type")
    List<ShiftType> selectList();

    @Select("SELECT * FROM shift_type WHERE id = #{id}")
    ShiftType selectById(Integer id);

    @Insert("INSERT INTO shift_type(name, color) VALUES(#{name}, #{color})")
    int insert(ShiftType shiftType);

    @Update("UPDATE shift_type SET name = #{name}, color = #{color} WHERE id = #{id}")
    int update(ShiftType shiftType);

    @Delete("DELETE FROM shift_type WHERE id = #{id}")
    int deleteById(Integer id);
}
