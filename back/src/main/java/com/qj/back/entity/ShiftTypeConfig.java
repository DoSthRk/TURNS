package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("shift_type_config")
public class ShiftTypeConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer consultantType;
    private String typeLabel;
    private Integer enabled;
    private Integer displayOrder;
}

