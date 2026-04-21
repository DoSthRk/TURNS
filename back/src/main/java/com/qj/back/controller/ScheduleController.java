package com.qj.back.controller;

import com.qj.back.Handler.Result;
import com.qj.back.annotation.Log;
import com.qj.back.entity.Schedule;
import com.qj.back.entity.ShiftType;
import com.qj.back.service.ScheduleService;
import com.qj.back.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@CrossOrigin
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping("/list")
    public Result<List<Schedule>> getScheduleList() {
        return scheduleService.getScheduleList();
    }

    @GetMapping("/shift-types")
    public Result<List<ShiftType>> getShiftTypes() {
        return scheduleService.getShiftTypes();
    }

    @Log(value = "添加排班", module = "排班管理")
    @PostMapping("/add")
    public Result<String> addSchedule(@RequestBody Schedule schedule, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                String account = JwtUtil.getAccountFromToken(token);
                return scheduleService.addSchedule(schedule, account);
            }
        }
        return new Result<>(401, "未登录或token已过期", null);
    }

    @Log(value = "删除排班", module = "排班管理")
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteSchedule(@PathVariable Integer id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                String account = JwtUtil.getAccountFromToken(token);
                return scheduleService.deleteSchedule(id, account);
            }
        }
        return new Result<>(401, "未登录或token已过期", null);
    }
    @PostMapping("/batch-add")
    public Result<String> batchAddSchedule(@RequestBody Map<String, List<Schedule>> request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                String account = JwtUtil.getAccountFromToken(token);
                return scheduleService.batchAddSchedule(request.get("schedules"), account);
            }
        }
        return new Result<>(401, "未登录或token已过期", null);
    }

    @GetMapping("/today")
    public Result<Schedule> getTodaySchedule(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                String account = JwtUtil.getAccountFromToken(token);
                return scheduleService.getTodaySchedule(account);
            }
        }
        return new Result<>(401, "未登录或token已过期", null);
    }
}
