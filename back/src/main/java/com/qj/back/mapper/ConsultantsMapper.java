package com.qj.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qj.back.entity.Consultants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ConsultantsMapper extends BaseMapper<Consultants> {

    @Update("UPDATE consultants SET order_status_normal = #{order_status_normal} WHERE id = #{id}")
    void updateStatusById(@Param("id")Integer id, @Param("status")Integer status);


    void updateShiftOrderById(@Param("id") Integer id, @Param("order_status") Integer orderStatus);

    @Select("SELECT * FROM consultants ORDER BY count_normal ASC, id ASC")
    List<Consultants> selectConsultantsOrderedByNormal();

    @Select("SELECT * FROM consultants ORDER BY count_sem ASC, id ASC")
    List<Consultants> selectConsultantsOrderedBySem();

    @Select("SELECT * FROM consultants ORDER BY count_single1 ASC, id ASC")
    List<Consultants> selectConsultantsOrderedBySingle1();
}

