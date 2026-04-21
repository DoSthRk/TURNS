package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_level_consultants")
public class ApLevelConsultant {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer countSingle1;

    private Integer status;

    private Integer waitingSingle1;

    private Integer orderStatusSingle1;

}
