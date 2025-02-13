package com.qj.back.controller;

import com.qj.back.Handler.Result;
import com.qj.back.entity.User;
import com.qj.back.entity.UserTodo;
import com.qj.back.service.ScheduleTodoService;
import com.qj.back.service.TodoService;
import com.qj.back.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.qj.back.entity.Schedule;
import com.qj.back.mapper.ScheduleMapper;
import com.qj.back.service.UserService;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/todo")
@CrossOrigin
public class TodoController {
    @Autowired
    private ScheduleTodoService scheduleTodoService;

    @Autowired
    private TodoService todoService;  // 添加 TodoService

    @Autowired
    private UserService userService;  // 添加 UserService

    @Autowired
    private ScheduleMapper scheduleMapper;  // 添加 ScheduleMapper


    @PostMapping("/init/{scheduleId}")
    public Result<String> initTodos(@PathVariable Integer scheduleId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                String account = JwtUtil.getAccountFromToken(token);
                // 验证是否是管理员
                User user = userService.getUserByAccount(account);
                if (!"admin".equals(user.getRole())) {
                    return new Result<>(403, "无权限", null);
                }

                Schedule schedule = scheduleMapper.selectById(scheduleId);
                if (schedule == null) {
                    return new Result<>(404, "排班不存在", null);
                }

                scheduleTodoService.initTodosForSchedule(schedule);
                return new Result<>(200, "初始化成功", null);
            }
        }
        return new Result<>(401, "未登录或token已过期", null);
    }
    @GetMapping("/list")
    public Result<List<UserTodo>> getTodoList(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                String account = JwtUtil.getAccountFromToken(token);
                return todoService.getTodoList(account);
            }
        }
        return new Result<>(401, "未登录或token已过期", null);
    }

    @PostMapping("/complete/{todoId}")
    public Result<String> completeTodo(@PathVariable Integer todoId,
                                       HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                String account = JwtUtil.getAccountFromToken(token);
                return todoService.completeTodo(todoId, account);
            }
        }
        return new Result<>(401, "未登录或token已过期", null);
    }
}
