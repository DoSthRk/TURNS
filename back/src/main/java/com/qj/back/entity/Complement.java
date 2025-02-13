package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.annotations.Insert;

@Data

@TableName("complement")
public class Complement {
    @TableId(type = IdType.AUTO)
    private Long complementId;  // 自增主键
    private Long id;           // 原始顾问ID
    private String name;
    private Integer type;
    private Integer waiting;
}


