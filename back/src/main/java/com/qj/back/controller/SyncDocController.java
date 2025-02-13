package com.qj.back.controller;

import com.qj.back.Handler.Result;
import com.qj.back.entity.SyncDoc;
import com.qj.back.service.SyncDocService;
import com.qj.back.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@SuppressWarnings("all")
@RequestMapping("/sync-doc")
@CrossOrigin
public class SyncDocController {
    @Autowired
    private SyncDocService syncDocService;

    @GetMapping("/content")
    public Result<?> getContent() {
        try {
            SyncDoc doc = syncDocService.getLatestContent();
            return new Result<>(200, "获取成功", doc);
        } catch (Exception e) {
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    @PostMapping("/content")
    public Result<?> updateContent(@RequestBody SyncDoc doc,
                                   HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        token = token.substring(7);
        if (!JwtUtil.validateToken(token)) {
            return new Result<>(401, "token已过期", null);
        }

        try {
            boolean success = syncDocService.updateContent(doc);
            if (success) {
                return new Result<>(200, "更新成功", null);
            } else {
                return new Result<>(500, "更新失败", null);
            }
        } catch (Exception e) {
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }
}
