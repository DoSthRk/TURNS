package com.qj.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SyncDoc {
    private Long id;
    private String content;
    private LocalDateTime updateTime;
    private String updateBy;
    private String updateByName;
}
