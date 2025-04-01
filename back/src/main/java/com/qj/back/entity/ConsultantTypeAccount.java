package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ConsultantTypeAccount {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer consultantTypeId;
    private Integer accountTypeId;
}
