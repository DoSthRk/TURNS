package com.qj.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qj.back.Handler.MyWebSocketHandler;
import com.qj.back.Handler.Result;
import com.qj.back.annotation.Log;
import com.qj.back.entity.ApLevelConsultant;
import com.qj.back.mapper.ApLevelConsultantMapper;
import com.qj.back.service.ApLevelConsultantService;
import com.qj.back.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/ap-level")
@Slf4j
public class ApLevelConsultantController {

    @Autowired
    private ApLevelConsultantMapper apLevelConsultantMapper;

    @Autowired
    private ApLevelConsultantService apLevelConsultantService;

    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    @GetMapping("/consultants")
    public Result<List<ApLevelConsultant>> getConsultants() {
        try {
            List<ApLevelConsultant> consultants = apLevelConsultantMapper.selectList(null);
            return new Result<>(200, "success", consultants);
        } catch (Exception e) {
            log.error("获取AP/Alevel顾问列表失败:", e);
            return new Result<>(500, "获取失败", null);
        }
    }

    @Log(value = "更新AP/Alevel顾问数据", module = "更新数据")
    @PostMapping("/update-count")
    public Result<String> updateCount(@RequestBody ApLevelConsultant consultant,
                                      HttpServletRequest request) {
        // 验证token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        token = token.substring(7);
        if (!JwtUtil.validateToken(token)) {
            return new Result<>(401, "token已过期", null);
        }

        try {
            // 更新数据库
            apLevelConsultantMapper.updateById(consultant);

            // WebSocket 推送
            ObjectMapper objectMapper = new ObjectMapper();
            String consultantJson = objectMapper.writeValueAsString(consultant);
            myWebSocketHandler.sendToAllClients(consultantJson);

            return new Result<>(200, "更新成功", null);
        } catch (Exception e) {
            log.error("更新失败:", e);
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }


    @PostMapping("/status")
    @Log(value = "更新AP/Alevel顾问状态", module = "顾问管理")
    public Result<Void> updateStatus(@RequestParam Integer consultantId,
                                     @RequestParam Integer status,
                                     HttpServletRequest request) {
        try {
            // 验证token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return new Result<>(401, "未登录或token已过期", null);
            }
            token = token.substring(7);
            if (!JwtUtil.validateToken(token)) {
                return new Result<>(401, "token已过期", null);
            }

            // 验证状态值是否有效
            if (status != 2 && status != 3) {
                return new Result<>(400, "无效的状态值", null);
            }

            apLevelConsultantService.updateStatus(Long.valueOf(consultantId), status);

            ApLevelConsultant updatedConsultant = apLevelConsultantMapper.selectById(consultantId);
            String consultantJson = new ObjectMapper().writeValueAsString(updatedConsultant);
            myWebSocketHandler.sendToAllClients(consultantJson);

            return new Result<>(200, "状态更新成功", null);
        } catch (Exception e) {
            log.error("更新状态失败:", e);
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }


}