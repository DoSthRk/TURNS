package com.qj.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qj.back.Handler.MyWebSocketHandler;
import com.qj.back.Handler.Result;
import com.qj.back.annotation.Log;
import com.qj.back.entity.Complement;
import com.qj.back.service.ComplementService;
import com.qj.back.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@SuppressWarnings("all")
@Slf4j
public class ComplementController {
    @Autowired
    private ComplementService complementService;

    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/complement")
    public Result<List<Complement>> getComplement() {
        try {
            List<Complement> complements = complementService.findAll();
            return new Result<>(200, "获取成功", complements);
        } catch (Exception e) {
            log.error("获取补充顾问列表失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    @Log(value = "添加补客资", module = "补客资管理")
    @PostMapping("/addComplement")
    public Result<?> addComplement(@RequestBody List<Complement> complements,
                                   HttpServletRequest request) {
        // ... token 验证代码 ...

        try {
            List<Complement> addedComplements = new ArrayList<>();
            for (Complement complement : complements) {
                boolean success = complementService.add(complement);
                if (success) {
                    addedComplements.add(complement);
                }
            }

            if (!addedComplements.isEmpty()) {
                // 确保消息格式正确
                String complementsJson = objectMapper.writeValueAsString(addedComplements);
                System.out.println("准备发送WebSocket消息: " + complementsJson);
                myWebSocketHandler.sendToAllClients(complementsJson);
            }

            return new Result<>(200, "添加成功", null);
        } catch (Exception e) {
            log.error("添加补充顾问失败:", e);
            return new Result<>(500, "添加失败: " + e.getMessage(), null);
        }
    }

    @Log(value = "更新补充顾问", module = "补充顾问管理")
    @PostMapping("/updateComplement")
    public Result<?> updateComplement(@RequestBody Complement complement,
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
            boolean success = complementService.update(complement);
            if (success) {
                // 获取更新后的完整数据
                Complement updatedComplement = complementService.getById(complement.getComplementId());
                // WebSocket推送完整数据
                String complementJson = objectMapper.writeValueAsString(updatedComplement);
                myWebSocketHandler.sendToAllClients(complementJson);
                return new Result<>(200, "更新成功", null);
            } else {
                return new Result<>(500, "更新失败", null);
            }
        } catch (Exception e) {
            log.error("更新补充顾问失败:", e);
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }

    @GetMapping("/complementByType")
    public Result<List<Complement>> getComplementByType(@RequestParam Integer type) {
        try {
            List<Complement> complements = complementService.findByType(type);
            return new Result<>(200, "获取成功", complements);
        } catch (Exception e) {
            log.error("按类型获取补充顾问失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }
    @PostMapping("/deleteComplement")
    public Result<?> deleteComplement(@RequestBody Complement complement,
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
            boolean success = complementService.delete(complement.getComplementId());
            if (success) {
                // WebSocket推送删除消息

                String complementJson = objectMapper.writeValueAsString(complement);
                myWebSocketHandler.sendToAllClients(complementJson);
                return new Result<>(200, "删除成功", null);
            } else {
                return new Result<>(500, "删除失败", null);
            }
        } catch (Exception e) {
            log.error("删除补充顾问失败:", e);
            return new Result<>(500, "删除失败: " + e.getMessage(), null);
        }
    }
    @Log(value = "清空补客资", module = "补客资管理")
    @PostMapping("/clearComplementByType")
    public Result<?> clearComplementByType(@RequestBody Map<String, Integer> params,
                                           HttpServletRequest request) {
        // 验证 token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        token = token.substring(7);
        if (!JwtUtil.validateToken(token)) {
            return new Result<>(401, "token已过期", null);
        }

        try {
            Integer type = params.get("type");
            if (type == null || (type != 1 && type != 2)) {
                return new Result<>(400, "无效的类型参数", null);
            }

            // 获取要删除的数据列表
            List<Complement> complementsToDelete = complementService.findByType(type);

            // 执行删除操作
            boolean success = complementService.deleteByType(type);

            if (success) {
                // 通过 WebSocket 通知其他客户端数据已被删除
                for (Complement complement : complementsToDelete) {
                    String complementJson = objectMapper.writeValueAsString(complement);
                    myWebSocketHandler.sendToAllClients(complementJson);
                }

                return new Result<>(200, "清除成功", null);
            } else {
                return new Result<>(500, "清除失败", null);
            }
        } catch (Exception e) {
            log.error("清除补充顾问失败:", e);
            return new Result<>(500, "清除失败: " + e.getMessage(), null);
        }
    }

}
