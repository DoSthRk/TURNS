package com.qj.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameScoreVO {
    private Integer rank;        // 排名
    private String username;     // 用户名
    private Integer score;       // 分数
    private LocalDateTime createTime;  // 创建时间
}
