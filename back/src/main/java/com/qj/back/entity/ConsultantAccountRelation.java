package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ConsultantAccountRelation {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer consultantId;    // 顾问ID
    private Integer accountTypeId;   // 账号类型ID
    private Integer count;           // 当前客资数量
    private Integer orderStatus;     // 排序状态
    private Integer waiting;         // 等待数量
}
