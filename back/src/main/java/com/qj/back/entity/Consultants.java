package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Set;

@Data
public class Consultants {
    private int id;
    private String name;
    private int type;
    @TableField(value = "count_normal")
    private int countNormal;
    @TableField(value = "count_sem")
    private int countSem;
    private int status;
    private int orderStatusNormal;
    private int orderStatusSem;
    private int orderStatusSingle1;
    private int countSingle1;
    private String description;

    @TableField(value = "waiting_normal")  // 添加数据库字段映射
    private int waitingNormal;
    @TableField(value = "waiting_sem")
    private int waitingSem;
    @TableField(value = "waiting_single1")
    private int waitingSingle1;
}
