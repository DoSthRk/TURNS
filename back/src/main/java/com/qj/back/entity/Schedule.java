package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    private Integer id;
    private String date;  // 改为String类型
    private Integer shiftTypeId;
    private Integer userId;
    private Integer createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    // 关联字段
    @TableField(exist = false)
    private ShiftType shiftType;
    @TableField(exist = false)
    private User user;
}
