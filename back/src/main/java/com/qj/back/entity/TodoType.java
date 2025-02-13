package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TodoType {
    private Integer id;
    private Integer shiftTypeId;
    private String content;

    @TableField(exist = false)
    private ShiftType shiftType;
}
