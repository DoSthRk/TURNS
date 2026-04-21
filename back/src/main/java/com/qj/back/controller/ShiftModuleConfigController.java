package com.qj.back.controller;

import com.qj.back.Handler.Result;
import com.qj.back.entity.ShiftModuleConfig;
import com.qj.back.entity.ShiftTypeConfig;
import com.qj.back.entity.User;
import com.qj.back.mapper.UserMapper;
import com.qj.back.service.ShiftModuleConfigService;
import com.qj.back.service.ShiftTypeConfigService;
import com.qj.back.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/shift-config")
@Slf4j
public class ShiftModuleConfigController {

    @Autowired
    private ShiftModuleConfigService shiftModuleConfigService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShiftTypeConfigService shiftTypeConfigService;

    @GetMapping("/type-options")
    public Result<List<ShiftTypeConfig>> getTypeOptions() {
        try {
            return new Result<>(200, "获取成功", shiftTypeConfigService.getAllTypeConfigs());
        } catch (Exception e) {
            log.error("Failed to get shift type options", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    @GetMapping("/type-options/enabled")
    public Result<List<ShiftTypeConfig>> getEnabledTypeOptions() {
        try {
            return new Result<>(200, "获取成功", shiftTypeConfigService.getEnabledTypeConfigs());
        } catch (Exception e) {
            log.error("Failed to get enabled shift type options", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    @PostMapping("/type-options")
    public Result<Void> saveTypeOptions(@RequestBody List<ShiftTypeConfig> configs, HttpServletRequest request) {
        Result<Void> authResult = verifyAdmin(request);
        if (authResult != null) {
            return authResult;
        }

        try {
            boolean success = shiftTypeConfigService.saveTypeConfigs(configs);
            if (success) {
                return new Result<>(200, "保存成功", null);
            }
            return new Result<>(500, "保存失败", null);
        } catch (Exception e) {
            log.error("Failed to save shift type options", e);
            return new Result<>(500, "保存失败: " + e.getMessage(), null);
        }
    }

    @GetMapping("/all")
    public Result<Map<Integer, List<ShiftModuleConfig>>> getAllConfigs() {
        try {
            Map<Integer, List<ShiftModuleConfig>> configs = shiftModuleConfigService.getAllByType();
            return new Result<>(200, "获取成功", configs);
        } catch (Exception e) {
            log.error("Failed to get shift configs", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    @GetMapping("/types/{consultantType}")
    public Result<List<ShiftModuleConfig>> getConfigsByType(@PathVariable Integer consultantType) {
        try {
            List<ShiftModuleConfig> configs = shiftModuleConfigService.getByConsultantType(consultantType);
            return new Result<>(200, "获取成功", configs);
        } catch (Exception e) {
            log.error("Failed to get shift configs for type {}", consultantType, e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    @PostMapping("/types/{consultantType}")
    public Result<Void> saveConfigsByType(@PathVariable Integer consultantType,
                                          @RequestBody List<ShiftModuleConfig> configs,
                                          HttpServletRequest request) {
        Result<Void> authResult = verifyAdmin(request);
        if (authResult != null) {
            return authResult;
        }

        try {
            boolean success = shiftModuleConfigService.saveByConsultantType(consultantType, configs);
            if (success) {
                return new Result<>(200, "保存成功", null);
            }
            return new Result<>(500, "保存失败", null);
        } catch (Exception e) {
            log.error("Failed to save shift configs for type {}", consultantType, e);
            return new Result<>(500, "保存失败: " + e.getMessage(), null);
        }
    }

    private Result<Void> verifyAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        token = token.substring(7);
        if (!JwtUtil.validateToken(token)) {
            return new Result<>(401, "token已过期", null);
        }

        String account = JwtUtil.getAccountFromToken(token);
        if (account == null || account.trim().isEmpty()) {
            return new Result<>(401, "token无效", null);
        }

        User user = userMapper.findByAccount(account);
        if (user == null) {
            return new Result<>(401, "用户不存在", null);
        }

        String role = user.getRole() == null ? "" : user.getRole().trim();
        if (!"admin".equalsIgnoreCase(role)) {
            return new Result<>(403, "仅管理员可配置轮班模块", null);
        }
        return null;
    }
}
