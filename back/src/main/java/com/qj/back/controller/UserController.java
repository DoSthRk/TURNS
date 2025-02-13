package com.qj.back.controller;

import com.qj.back.Handler.Result;
import com.qj.back.Handler.ResultUtil;
import com.qj.back.entity.User;
import com.qj.back.service.Impl.UserServiceImpl;
import com.qj.back.service.UserService;
import com.qj.back.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@SuppressWarnings("all")
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserServiceImpl userServiceImpl;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> loginData) {
        String account = loginData.get("account");
        String password = loginData.get("password");

        Result<Map<String, String>> loginResult = userServiceImpl.loginService(account, password);

        if (loginResult.getCode() == 200) {
            String token = JwtUtil.generateToken(account);
            User user = userServiceImpl.getUserByAccount(account);
            // 获取登录结果中的数据
            Map<String, String> data = loginResult.getData();
            // 添加token
            data.put("token", token);
            data.put("account", user.getAccount());
            data.put("avatarUrl", user.getAvatarUrl());  // 使用驼峰命名
            data.put("role", user.getRole() != null ? user.getRole() : "user");  // 添加默认角色

            return ResultUtil.success("登录成功", data);
        } else {
            return ResultUtil.error(loginResult.getMsg());
        }
    }

    @RequestMapping("/register")
    public Result<String> register(@RequestBody User user) {
        String msg = userServiceImpl.registerService(user);
        if ("SUCCESS".equals(msg)) {
            return ResultUtil.success("注册成功","registration data");
        } else {
            return ResultUtil.error(msg);
        }
    }

    @GetMapping("/checkToken")
    public Result<Map<String, String>> checkToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                String account = JwtUtil.getAccountFromToken(token);
                User user = userServiceImpl.getUserByAccount(account);
                Map<String, String> data = new HashMap<>();
                data.put("account", user.getAccount());
                data.put("avatarUrl", user.getAvatarUrl());
                data.put("role", user.getRole() != null ? user.getRole() : "user");  // 添加角色信息
                System.out.println("Login response data: " + data);


                return ResultUtil.success("token有效", data);
            }
        }
        return ResultUtil.error("token无效");
    }

    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestHeader("Authorization") String token,
                                       @RequestBody Map<String, String> passwordData) {
        token = token.replace("Bearer ", "");
        String account = JwtUtil.getAccountFromToken(token);
        String newPassword = passwordData.get("newPassword");
        
        String msg = userServiceImpl.changePassword(account, newPassword);
        if ("SUCCESS".equals(msg)) {
            return ResultUtil.success("密码修改成功", null);
        }
        return ResultUtil.error(msg);
    }

    @PostMapping("/uploadAvatar")
    public Result<Map<String, String>> uploadAvatar(@RequestHeader("Authorization") String token,
                                                   @RequestParam("file") MultipartFile file) {
        token = token.replace("Bearer ", "");
        String account = JwtUtil.getAccountFromToken(token);
        String avatarUrl = userServiceImpl.saveAvatar(account, file);
        
        if (avatarUrl != null) {
            Map<String, String> data = new HashMap<>();
            data.put("avatarUrl", avatarUrl);
            return ResultUtil.success("头像上传成功", data);
        }
        return ResultUtil.error("头像上传失败");
    }

    @GetMapping("/list")
    public Result<List<User>> getUserList() {
        try {
            List<User> users = userServiceImpl.getUserList();
            return ResultUtil.success("获取成功", users);
        } catch (Exception e) {
            return ResultUtil.error("获取用户列表失败: " + e.getMessage());
        }
    }

}
