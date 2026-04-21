package com.qj.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qj.back.entity.Consultants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface ConsultantsMapper extends BaseMapper<Consultants> {

    @Update("UPDATE consultants SET order_status_normal = #{order_status_normal} WHERE id = #{id}")
    void updateStatusById(@Param("id")Integer id, @Param("status")Integer status);


    void updateShiftOrderById(@Param("id") Integer id, @Param("order_status") Integer orderStatus);
    // 添加新方法用于更新 status 字段
    @Update("UPDATE consultants SET status = #{status} WHERE id = #{id}")
    void updateWorkStatus(@Param("id")Integer id, @Param("status")Integer status);

    @Select("SELECT * FROM consultants ORDER BY count_normal ASC, id ASC")
    List<Consultants> selectConsultantsOrderedByNormal();

    @Select("SELECT * FROM consultants ORDER BY count_sem ASC, id ASC")
    List<Consultants> selectConsultantsOrderedBySem();

    @Select("SELECT * FROM consultants ORDER BY count_single1 ASC, id ASC")
    List<Consultants> selectConsultantsOrderedBySingle1();

    @Select("SELECT * FROM consultants WHERE type = #{type} ORDER BY sort_order ASC, id ASC")
    List<Consultants> selectConsultantsByTypeOrderedBySortOrder(@Param("type") Integer type);

    @Select("SELECT * FROM consultants WHERE type = #{type} AND status = 1")
    List<Consultants> selectActiveConsultantsByType(@Param("type") Integer type);

    @Select("SELECT MAX(count_normal) as normalMax, " +
            "MAX(count_sem) as semMax, " +
            "MAX(count_single1) as single1Max " +
            "FROM consultants WHERE type = #{type} AND status = 1")
    Map<String, Integer> selectMaxCountsByType(@Param("type") Integer type);


    /**
     * 清零指定类型且状态正常的顾问的官号客资数
     * @param type 顾问类型
     */
    @Update("UPDATE consultants SET count_normal = 0 WHERE status = 1 AND type = #{type}")
    void clearNormalCount(Integer type);
}
