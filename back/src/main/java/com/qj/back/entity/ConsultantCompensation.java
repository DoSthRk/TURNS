package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("consultant_compensation")
public class ConsultantCompensation {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private Integer consultantId;
    private Integer type;
    private Integer normalCount;
    private LocalDateTime createTime;
    private Integer status;
    private Integer difference;  // 添加 difference 字段

    @TableField(exist = false)
    private String consultantName;  // 关联查询用
}
