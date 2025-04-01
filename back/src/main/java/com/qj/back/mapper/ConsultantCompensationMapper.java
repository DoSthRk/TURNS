package com.qj.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qj.back.entity.ConsultantCompensation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface ConsultantCompensationMapper extends BaseMapper<ConsultantCompensation> {
    @Select("SELECT cc.*, c.name as consultant_name " +
            "FROM consultant_compensation cc " +
            "LEFT JOIN consultants c ON cc.consultant_id = c.id " +
            "WHERE cc.status = 1 " +
            "ORDER BY cc.create_time DESC")
    List<ConsultantCompensation> selectPendingCompensations();

    @Select("SELECT COUNT(*) FROM consultant_compensation " +
            "WHERE consultant_id = #{consultantId} AND status = 1")
    int countPendingByConsultantId(@Param("consultantId") Integer consultantId);

    // 新增插入统计数据的方法
    @Insert("INSERT INTO consultant_compensation (consultant_id, type, normal_count, create_time, status, difference) " +
            "VALUES (#{consultantId}, #{type}, #{normalCount}, #{createTime}, #{status}, #{difference})")
    void insertCompensationStatistics(@Param("consultantId") Integer consultantId,
                                      @Param("type") Integer type,
                                      @Param("normalCount") Integer normalCount,
                                      @Param("createTime") Date createTime,
                                      @Param("status") Integer status,
                                      @Param("difference") Integer difference);
}
