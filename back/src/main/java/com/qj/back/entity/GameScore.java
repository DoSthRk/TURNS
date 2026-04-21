package com.qj.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("game_score")  // 添加这个注解
public class GameScore {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private String gameType;

    private Integer score;

    private LocalDateTime createTime;
}

