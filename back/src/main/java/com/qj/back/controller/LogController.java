package com.qj.back.controller;

import com.qj.back.Handler.Result;
import com.qj.back.entity.OperationLog;
import com.qj.back.entity.User;
import com.qj.back.mapper.OperationLogMapper;
import com.qj.back.service.UserService;
import com.qj.back.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/log")
@CrossOrigin
public class LogController {
    @Autowired
    private OperationLogMapper operationLogMapper;
    @Autowired
    private UserService userService;  // 添加 UserService
    @GetMapping("/list")
    public Result<List<OperationLog>> getLogList(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                return new Result<>(200, "获取成功", operationLogMapper.selectList());
            }
        }
        return new Result<>(401, "未登录或token已过期", null);
    }
}
