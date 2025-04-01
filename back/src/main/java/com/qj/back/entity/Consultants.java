package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Consultants {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    @TableField(exist = false)
    private String password;
    @TableField(exist = false)
    private String phoneNumber;
    private String description;
    private Integer status;
    private Integer type;
    private Integer sortOrder;
    
    // 支持的账号类型列表，非数据库字段
    @TableField(exist = false)
    private List<Integer> supportedAccountTypes;
    
    // 与每种账号类型关联的状态信息，非数据库字段
    @TableField(exist = false)
    private Map<Integer, ConsultantAccountRelation> accountTypeStatus;
    
    // 以下字段保留用于兼容现有代码，新代码应使用accountTypeStatus中的关联关系
    private Integer countNormal;
    private Integer orderStatusNormal;
    private Integer waitingNormal;
    
    private Integer countSem;
    private Integer orderStatusSem;
    private Integer waitingSem;
    
    private Integer countSingle1;
    private Integer orderStatusSingle1;
    private Integer waitingSingle1;
}
