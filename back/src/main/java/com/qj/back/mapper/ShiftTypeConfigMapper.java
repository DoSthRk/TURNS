package com.qj.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qj.back.entity.ShiftTypeConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShiftTypeConfigMapper extends BaseMapper<ShiftTypeConfig> {

    @Select("SELECT * FROM shift_type_config ORDER BY display_order ASC, consultant_type ASC, id ASC")
    List<ShiftTypeConfig> selectAllOrdered();

    @Select("SELECT * FROM shift_type_config WHERE enabled = 1 ORDER BY display_order ASC, consultant_type ASC, id ASC")
    List<ShiftTypeConfig> selectEnabledOrdered();
}

