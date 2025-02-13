package com.qj.back.service.Impl;

import com.qj.back.Handler.Result;
import com.qj.back.entity.User;
import com.qj.back.mapper.UserMapper;
import com.qj.back.service.UserService;
import com.qj.back.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-path}")
    private String accessPath;

    @Override
    public Result<Map<String, String>> loginService(String account, String password) {
        User user = userMapper.findByAccount(account);
        if (user == null) {
            return new Result<>(400, "账号不存在", null);
        }
        if (!password.equals(user.getPassword())) {
            return new Result<>(400, "密码错误", null);
        }

        // 生成token并返回用户信息
        Map<String, String> data = new HashMap<>();
        data.put("token", JwtUtil.generateToken(account));
        data.put("role", user.getRole() != null ? user.getRole() : "user");
        data.put("account", user.getAccount());

        return new Result<>(200, "登录成功", data);
    }

    @Override
    public String registerService(User user) {
        if (userMapper.findByAccount(user.getAccount()) != null) {
            return "账号已存在";
        }
        userMapper.insert(user);
        return "SUCCESS";
    }

    @Override
    public String changePassword(String account, String newPassword) {
        User user = userMapper.findByAccount(account);
        if (user == null) {
            return "用户不存在";
        }
        user.setPassword(newPassword);
        userMapper.updatePassword(user);
        return "SUCCESS";
    }

    @Override
    public String saveAvatar(String account, MultipartFile file) {
        User user = userMapper.findByAccount(account);
        if (user == null) {
            return "用户不存在";
        }

        String fileName = UUID.randomUUID().toString() + 
            file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String filePath = uploadPath + fileName;

        try {
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
            
            String avatarUrl = accessPath + fileName;
            user.setAvatarUrl(avatarUrl);
            userMapper.updateAvatar(user);
            
            return avatarUrl;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User getUserByAccount(String account) {
        return userMapper.findByAccount(account);
    }

    @Override
    public User getUserById(Integer id) {
        return userMapper.selectById(id);
    }
    @Override
    public List<User> getUserList() {
        return userMapper.selectList();
    }
}
