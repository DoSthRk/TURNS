package com.qj.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qj.back.Handler.MyWebSocketHandler;
import com.qj.back.Handler.Result;
import com.qj.back.annotation.Log;
import com.qj.back.entity.AccountType;
import com.qj.back.entity.Consultants;
import com.qj.back.service.AccountTypeService;
import com.qj.back.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/account-type")
@Slf4j
public class AccountTypeController {

    @Autowired
    private AccountTypeService accountTypeService;
    
    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    /**
     * 获取所有账号类型
     */
    @GetMapping("/list")
    public Result<List<AccountType>> getAllAccountTypes() {
        try {
            List<AccountType> accountTypes = accountTypeService.getAllEnabledAccountTypes();
            return new Result<>(200, "获取成功", accountTypes);
        } catch (Exception e) {
            log.error("获取账号类型列表失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    /**
     * 按分组获取账号类型
     */
    @GetMapping("/groups")
    public Result<Map<String, List<AccountType>>> getAccountTypeGroups() {
        try {
            Map<String, List<AccountType>> groupedTypes = accountTypeService.getAccountTypesByGroups();
            return new Result<>(200, "获取成功", groupedTypes);
        } catch (Exception e) {
            log.error("获取账号类型分组失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取账号类型详情（包含关联的顾问）
     */
    @GetMapping("/detail/{id}")
    public Result<AccountType> getAccountTypeDetail(@PathVariable Integer id) {
        try {
            AccountType accountType = accountTypeService.getAccountTypeWithConsultants(id);
            if (accountType != null) {
                return new Result<>(200, "获取成功", accountType);
            } else {
                return new Result<>(404, "账号类型不存在", null);
            }
        } catch (Exception e) {
            log.error("获取账号类型详情失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    /**
     * 添加或更新账号类型
     */
    @Log(value = "添加/更新账号类型", module = "系统管理")
    @PostMapping("/save")
    public Result<Void> saveAccountType(@RequestBody AccountType accountType, HttpServletRequest request) {
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

            boolean success = accountTypeService.saveOrUpdateAccountType(accountType);
            if (success) {
                return new Result<>(200, "保存成功", null);
            } else {
                return new Result<>(500, "保存失败", null);
            }
        } catch (Exception e) {
            log.error("保存账号类型失败:", e);
            return new Result<>(500, "保存失败: " + e.getMessage(), null);
        }
    }

    /**
     * 启用/禁用账号类型
     */
    @Log(value = "更新账号类型状态", module = "系统管理")
    @PostMapping("/status")
    public Result<Void> updateAccountTypeStatus(
            @RequestParam Integer id,
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

            boolean success = accountTypeService.toggleAccountTypeStatus(id, status);
            if (success) {
                return new Result<>(200, status == 1 ? "启用成功" : "禁用成功", null);
            } else {
                return new Result<>(404, "账号类型不存在", null);
            }
        } catch (Exception e) {
            log.error("更新账号类型状态失败:", e);
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }

    /**
     * 关联顾问到账号类型
     */
    @Log(value = "关联顾问到账号类型", module = "系统管理")
    @PostMapping("/associate-consultants")
    public Result<Void> associateConsultants(
            @RequestParam Integer accountTypeId,
            @RequestBody List<Integer> consultantIds,
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

            boolean success = accountTypeService.associateConsultantsToAccountType(accountTypeId, consultantIds);
            if (success) {
                // 获取更新后的数据并通过WebSocket推送
                try {
                    AccountType updatedAccountType = accountTypeService.getAccountTypeWithConsultants(accountTypeId);
                    String accountTypeJson = new ObjectMapper().writeValueAsString(updatedAccountType);
                    myWebSocketHandler.sendToAllClients(accountTypeJson);
                } catch (Exception e) {
                    log.error("推送更新数据失败:", e);
                }
                
                return new Result<>(200, "关联成功", null);
            } else {
                return new Result<>(500, "关联失败", null);
            }
        } catch (Exception e) {
            log.error("关联顾问到账号类型失败:", e);
            return new Result<>(500, "关联失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取指定账号类型的顾问列表（已排序）
     */
    @GetMapping("/consultants/{accountTypeId}")
    public Result<List<Consultants>> getConsultantsByAccountType(@PathVariable Integer accountTypeId) {
        try {
            List<Consultants> consultants = accountTypeService.getConsultantsByAccountType(accountTypeId);
            return new Result<>(200, "获取成功", consultants);
        } catch (Exception e) {
            log.error("获取账号类型的顾问列表失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取所有账号类型分组
     */
    @GetMapping("/group-list")
    public Result<List<String>> getAllGroups() {
        try {
            List<String> groups = accountTypeService.getAllAccountTypeGroups();
            return new Result<>(200, "获取成功", groups);
        } catch (Exception e) {
            log.error("获取账号类型分组列表失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    /**
     * 添加新分组
     */
    @Log(value = "添加新分组", module = "系统管理")
    @PostMapping("/add-group")
    public Result<Void> addGroup(@RequestParam String groupName, HttpServletRequest request) {
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

            boolean success = accountTypeService.addGroup(groupName);
            if (success) {
                return new Result<>(200, "添加成功", null);
            } else {
                return new Result<>(500, "分组已存在", null);
            }
        } catch (Exception e) {
            log.error("添加分组失败:", e);
            return new Result<>(500, "添加失败: " + e.getMessage(), null);
        }
    }

    /**
     * 更新分组名称
     */
    @Log(value = "更新分组名称", module = "系统管理")
    @PostMapping("/update-group")
    public Result<Void> updateGroup(
            @RequestParam String oldName,
            @RequestParam String newName,
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

            boolean success = accountTypeService.renameGroup(oldName, newName);
            if (success) {
                return new Result<>(200, "更新成功", null);
            } else {
                return new Result<>(500, "分组不存在或新名称已存在", null);
            }
        } catch (Exception e) {
            log.error("更新分组名称失败:", e);
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }

    /**
     * 删除分组
     */
    @Log(value = "删除分组", module = "系统管理")
    @PostMapping("/delete-group")
    public Result<Void> deleteGroup(@RequestParam String groupName, HttpServletRequest request) {
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

            boolean hasAccountTypes = accountTypeService.groupHasAccountTypes(groupName);
            if (hasAccountTypes) {
                return new Result<>(400, "分组下存在账号类型，无法删除", null);
            }

            boolean success = accountTypeService.deleteGroup(groupName);
            if (success) {
                return new Result<>(200, "删除成功", null);
            } else {
                return new Result<>(500, "分组不存在", null);
            }
        } catch (Exception e) {
            log.error("删除分组失败:", e);
            return new Result<>(500, "删除失败: " + e.getMessage(), null);
        }
    }

    /**
     * 根据分组名称获取该分组下所有账号类型（包括隐藏的）
     */
    @GetMapping("/by-group")
    public Result<List<AccountType>> getAccountTypesByGroupName(@RequestParam String groupName) {
        try {
            List<AccountType> accountTypes = accountTypeService.getAccountTypesByGroupName(groupName);
            return new Result<>(200, "获取成功", accountTypes);
        } catch (Exception e) {
            log.error("获取分组下账号类型失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    /**
     * 删除账号类型
     */
    @Log(value = "删除账号类型", module = "系统管理")
    @PostMapping("/delete/{accountTypeId}")
    public Result<Void> deleteAccountType(@PathVariable Integer accountTypeId, HttpServletRequest request) {
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

            boolean success = accountTypeService.deleteAccountType(accountTypeId);
            if (success) {
                return new Result<>(200, "删除成功", null);
            } else {
                return new Result<>(500, "账号类型不存在或删除失败", null);
            }
        } catch (Exception e) {
            log.error("删除账号类型失败:", e);
            return new Result<>(500, "删除失败: " + e.getMessage(), null);
        }
    }
}
