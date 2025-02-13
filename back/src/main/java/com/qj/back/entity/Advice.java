package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.apache.ibatis.mapping.FetchType;

import java.util.Date;

@Data
public class Advice {
    private String text;
    private int id;
    private Integer userId;
    private Date createTime;

    private User user;
}
