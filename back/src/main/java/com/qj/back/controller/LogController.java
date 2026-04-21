package com.qj.back.controller;

import com.qj.back.Handler.Result;
import com.qj.back.entity.OperationLog;
import com.qj.back.mapper.OperationLogMapper;
import com.qj.back.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/log")
@CrossOrigin
public class LogController {
    @Autowired
    private OperationLogMapper operationLogMapper;

    @GetMapping("/list")
    public Result<List<OperationLog>> getLogList(HttpServletRequest request) {
        if (!isTokenValid(request)) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        List<OperationLog> records = operationLogMapper.selectPage(0, 200, null, null, null, null);
        return new Result<>(200, "获取成功", records);
    }

    @GetMapping("/page")
    public Result<Map<String, Object>> getLogPage(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "20") Integer size,
                                                  @RequestParam(required = false) String account,
                                                  @RequestParam(required = false) String module,
                                                  @RequestParam(required = false) String startTime,
                                                  @RequestParam(required = false) String endTime) {
        if (!isTokenValid(request)) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 200);
        int offset = (safePage - 1) * safeSize;

        Date startDate = parseDate(startTime);
        Date endDate = parseDate(endTime);

        long total = operationLogMapper.count(account, module, startDate, endDate);
        List<OperationLog> records = total > 0
                ? operationLogMapper.selectPage(offset, safeSize, account, module, startDate, endDate)
                : Collections.emptyList();

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("page", safePage);
        data.put("size", safeSize);
        return new Result<>(200, "获取成功", data);
    }

    private boolean isTokenValid(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        token = token.substring(7);
        return JwtUtil.validateToken(token);
    }

    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
}
