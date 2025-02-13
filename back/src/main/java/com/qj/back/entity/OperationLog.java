package com.qj.back.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OperationLog {
    private Integer id;
    private String account;      // 操作人账号
    private String operation;    // 操作内容
    private String module;       // 操作模块
    private String method;       // 操作方法
    private String params;       // 操作参数
    private String ip;          // 操作IP
    private Date createTime;    // 操作时间
}
