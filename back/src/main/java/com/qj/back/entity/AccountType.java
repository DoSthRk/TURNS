package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

@Data
public class AccountType {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String typeName;        // 账号类型名称：辅导官号、申诉官号、SEM辅导、SEM申诉、AP/Alevel、抖音等
    private String description;     // 描述信息
    private Integer status;         // 状态: 0-禁用, 1-启用
    
    // 对应现有系统中的字段名，用于兼容现有代码
    private String countFieldName;          // 例如: countNormal, countSem, countSingle1
    private String orderStatusFieldName;    // 例如: orderStatusNormal, orderStatusSem, orderStatusSingle1
    private String waitingFieldName;        // 例如: waitingNormal, waitingSem, waitingSingle1
    
    // 用于前端显示的分组信息
    private String groupName;       // 分组名称：辅导、申诉、AP/Alevel等
    private Integer displayOrder;   // 显示顺序
    
    @TableField(exist = false)
    private List<Integer> consultantIds;    // 关联到此账号类型的顾问ID列表，非数据库字段
}
