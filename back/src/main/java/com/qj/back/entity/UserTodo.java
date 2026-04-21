package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTodo {
    private Integer id;
    private Integer userId;
    private Integer todoTypeId;
    private Integer scheduleId;
    private Boolean isCompleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date completeTime;

    @TableField(exist = false)
    private TodoType todoType;
    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private Schedule schedule;
}
