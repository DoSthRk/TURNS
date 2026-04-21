package com.qj.back.entity;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String account;
    private String password;
    private String avatarUrl;
    private String role = "user";  // 默认角色为普通用户


    // getters and setters
}
