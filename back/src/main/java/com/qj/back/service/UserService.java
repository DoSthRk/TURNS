package com.qj.back.service;

import com.qj.back.Handler.Result;
import com.qj.back.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    Result<Map<String, String>> loginService(String account, String password);
    String registerService(User user);
    String changePassword(String account, String newPassword);
    String saveAvatar(String account, MultipartFile file);
    User getUserByAccount(String account);
    User getUserById(Integer id);
    List<User> getUserList();
}

