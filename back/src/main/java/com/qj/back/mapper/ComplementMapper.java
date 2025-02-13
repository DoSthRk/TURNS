package com.qj.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qj.back.entity.Complement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ComplementMapper extends BaseMapper<Complement> {
    @Select("SELECT * FROM complement")
    List<Complement> findAll();

    @Insert("INSERT INTO complement (id, name, type, waiting) " +
            "VALUES (#{id}, #{name}, #{type}, #{waiting})")
    @Options(useGeneratedKeys = true, keyProperty = "complementId")  // 自动获取生成的complementId
    int insert(Complement complement);

    @Update("UPDATE complement SET waiting = #{waiting} " +  // 修复了SQL语句中的空格
            "WHERE complement_id = #{complementId}")
    int update(Complement complement);

    @Select("SELECT * FROM complement WHERE type = #{type}")
    List<Complement> selectByType(Integer type);

    @Select("SELECT type FROM consultants WHERE id = #{id}")
    Integer getConsultantType(Long id);

    @Delete("DELETE FROM complement WHERE complement_id = #{complementId}")
    int deleteById(Long complementId);

    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    // 添加新方法：根据类型删除数据
    @Delete("DELETE FROM complement WHERE type = #{type}")
    int deleteByType(@Param("type") Integer type);

    // 添加新方法：根据类型统计数量
    @Select("SELECT COUNT(*) FROM complement WHERE type = #{type}")
    int countByType(@Param("type") Integer type);

    // 添加新方法：根据 complementId 获取完整数据
    @Select("SELECT * FROM complement WHERE complement_id = #{complementId}")
    Complement getById(@Param("complementId") Long complementId);
}
