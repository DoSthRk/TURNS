package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("shift_module_config")
public class ShiftModuleConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer consultantType;
    private String moduleKey;
    private String moduleLabel;

    private String countFieldName;
    private String orderStatusFieldName;
    private String waitingFieldName;

    private Integer enabled;
    private Integer displayOrder;
}

