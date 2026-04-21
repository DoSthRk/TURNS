package com.qj.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.qj.back.Handler.MyWebSocketHandler;
import com.qj.back.Handler.Result;
import com.qj.back.entity.Advice;
import com.qj.back.mapper.AdviceMapper;
import com.qj.back.service.AdviceService;
import com.qj.back.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/advice")
@RequiredArgsConstructor
@CrossOrigin

public class AdviceController {

    private final AdviceService adviceService;


    private String getAccountFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                return JwtUtil.getAccountFromToken(token);
            }
        }
        return null;
    }

    /**
     * 添加留言
     */
    @PostMapping("/add")
    public Result<String> addAdvice(@RequestBody Advice advice, HttpServletRequest request) {
        // 验证token并获取用户账号
        String account = getAccountFromRequest(request);
        if (account == null) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        // 添加留言
        return adviceService.addAdvice(advice, account);
    }

    /**
     * 获取所有留言
     */
    @GetMapping("/list")
    public Result<List<Advice>> getAllAdvice() {
        return adviceService.getAllAdvice();
    }

    /**
     * 删除留言
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteAdvice(@PathVariable Integer id, HttpServletRequest request) {
        // 验证token并获取用户账号
        String account = getAccountFromRequest(request);
        if (account == null) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        return adviceService.deleteAdvice(id);
    }

    /**
     * 获取指定用户的所有留言（可选功能）
     */
    @GetMapping("/user")
    public Result<List<Advice>> getUserAdvice(HttpServletRequest request) {
        String account = getAccountFromRequest(request);
        if (account == null) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        return adviceService.getAdviceByAccount(account);
    }
}
