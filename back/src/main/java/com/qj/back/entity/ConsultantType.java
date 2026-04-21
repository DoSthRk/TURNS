package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

@Data
public class ConsultantType {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String typeName;
    private String description;
    private Integer status; // 状态: 0-禁用, 1-启用
    
    @TableField(exist = false)
    private List<AccountType> supportedAccountTypes; // 该顾问类型支持的账号类型列表
}
