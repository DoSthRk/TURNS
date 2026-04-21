package com.qj.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qj.back.entity.ShiftModuleConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShiftModuleConfigMapper extends BaseMapper<ShiftModuleConfig> {

    @Select("SELECT * FROM shift_module_config WHERE consultant_type = #{consultantType} ORDER BY display_order ASC, id ASC")
    List<ShiftModuleConfig> selectByConsultantType(Integer consultantType);

    @Select("SELECT * FROM shift_module_config ORDER BY consultant_type ASC, display_order ASC, id ASC")
    List<ShiftModuleConfig> selectAllOrdered();
}

