package com.qj.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.qj.back.Handler.MyWebSocketHandler;
import com.qj.back.Handler.Result;
import com.qj.back.annotation.Log;
import com.qj.back.entity.ConsultantAccountRelation;
import com.qj.back.entity.ConsultantCompensation;
import com.qj.back.entity.Consultants;
import com.qj.back.mapper.ConsultantAccountRelationMapper;
import com.qj.back.mapper.ConsultantCompensationMapper;
import com.qj.back.mapper.ConsultantsMapper;
import com.qj.back.service.AccountTypeService;
import com.qj.back.service.ConsultantsService;
import com.qj.back.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@SuppressWarnings("all")
@Slf4j
public class ConsultantsController {

    @Autowired
    private ConsultantCompensationMapper compensationMapper;

    @Autowired
    private ConsultantsMapper consultantsMapper;

    @Autowired
    private ConsultantsService consultantsService;

    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    @Autowired
    private AccountTypeService accountTypeService;

    @Autowired
    private ConsultantAccountRelationMapper consultantAccountRelationMapper;

    private Gson gson = new Gson();

    @GetMapping("/consultants")
    public String getConsultants() {
        System.out.println("获取顾问列表请求");
        List<Consultants> consultants = consultantsMapper.selectList(null);
        System.out.println("数据库返回顾问数量: " + consultants.size());
        if (consultants.isEmpty()) {
            System.out.println("警告: 数据库没有返回任何顾问数据");
        } else {
            System.out.println("第一条顾问数据: " + gson.toJson(consultants.get(0)));
        }
        return gson.toJson(consultants);
    }
    @GetMapping("/consultantsByNormal")
    public String getConsultantsByNormal() {
        List<Consultants> consultants = consultantsMapper.selectConsultantsOrderedByNormal();
        System.out.println(gson.toJson(consultants));
        return gson.toJson(consultants);
    }

    @GetMapping("/consultantsBySem")
    public String getConsultantsBySem() {
        List<Consultants> consultants = consultantsMapper.selectConsultantsOrderedBySem();
        System.out.println(gson.toJson(consultants));
        return gson.toJson(consultants);
    }

    @GetMapping("/consultantsBySingle1")
    public String getConsultantsBySingle1() {
        List<Consultants> consultants = consultantsMapper.selectConsultantsOrderedBySingle1();
        System.out.println(gson.toJson(consultants));
        return gson.toJson(consultants);
    }



    @PostMapping("/addConsultants")
    public String addConsultants(@RequestBody Consultants consultants) {
        System.out.println("添加顾问: " + gson.toJson(consultants));
        consultantsMapper.insert(consultants);
        return gson.toJson(consultants);
    }

    @PostMapping("/deleteConsultants")
    public void deleteConsultants(@RequestBody Consultants consultants) {
        consultantsMapper.deleteById(consultants.getId());
    }

    @Log(value = "更新数据", module = "更新数据")
    @PostMapping("/updateConsultants")
    public Result<String> updateConsultants(@RequestBody Consultants consultants,
                                            @RequestParam String operationType,
                                            HttpServletRequest request) {
        // 1. 首先验证token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new Result<>(401, "未登录或token已过期", null);
        }

        token = token.substring(7);
        if (!JwtUtil.validateToken(token)) {
            return new Result<>(401, "token已过期", null);
        }

        try {
            // 2. 处理状态
            if(consultants.getStatus() == 0){
                consultants.setOrderStatusNormal(1);
                consultants.setOrderStatusSem(1);
                consultants.setOrderStatusSingle1(1);
            }

            // 5. 更新数据库
            consultantsMapper.updateById(consultants);

            // 6. WebSocket 推送
            ObjectMapper objectMapper = new ObjectMapper();
            String consultantJson = objectMapper.writeValueAsString(consultants);
            myWebSocketHandler.sendToAllClients(consultantJson);

            return new Result<>(200, "更新成功", null);
        } catch (Exception e) {
            log.error("更新失败:", e);
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }
//
//    @PostMapping("/changeStatus")
//    public void changeStatus(@RequestBody Consultants consultants) {
//        System.out.println("更新前的信息："+gson.toJson(consultants));
//
//        consultantsMapper.updateStatusById(consultants.getId(), consultants.getStatus());
//
//        Consultants updatedConsultants = consultantsMapper.selectById(consultants.getId());
//        System.out.println("更新后的信息："+gson.toJson(updatedConsultants));
//
//        // 2. 推送更新后的顾问信息给所有 WebSocket 客户端
//        try {
//            String updatedConsultantJson = new ObjectMapper().writeValueAsString(updatedConsultants);
//
//            myWebSocketHandler.sendToAllClients(updatedConsultantJson);  // 通过 WebSocket 推送数据
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

//    @PostMapping("/saveShiftOrder")
//    public void saveShiftOrder(@RequestBody List<Consultants> consultantsList){
//        for (Consultants consultant : consultantsList) {
//            consultantsMapper.updateShiftOrderById(consultant.getId(),consultant.getOrderStatusNormal());
//        }
//        try{
//            String updatedConsultantJson = new ObjectMapper().writeValueAsString(consultantsList);
//            myWebSocketHandler.sendToAllClients(updatedConsultantJson);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    @PostMapping("/updateOrder")
//    public void updateOrder(@RequestBody Consultants consultant) {
//        consultantsMapper.updateShiftOrderById(consultant.getId(),consultant.getOrderStatusNormal());
//        try{
//            String updatedConsultantJson = new ObjectMapper().writeValueAsString(consultant);
//            myWebSocketHandler.sendToAllClients(updatedConsultantJson);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    @PostMapping("/restore")
    @Log(value = "恢复顾问状态", module = "顾问管理")
    public Result<Void> restoreConsultant(@RequestParam Integer consultantId,
                                          @RequestParam Integer fromStatus,
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

            // 根据状态类型调用不同的恢复方法
            if (fromStatus == 2) {  // 从暂停恢复
                consultantsService.restoreFromPause(consultantId);
            } else if (fromStatus == 3) {  // 从休息恢复
                consultantsService.restoreFromBreak(consultantId);
            } else {
                return new Result<>(400, "无效的状态类型", null);
            }
            Consultants updatedConsultant = consultantsMapper.selectById(consultantId);
            String consultantJson = new ObjectMapper().writeValueAsString(updatedConsultant);
            myWebSocketHandler.sendToAllClients(consultantJson);
            return new Result<>(200, "状态恢复成功", null);
        } catch (Exception e) {
            log.error("恢复状态失败:", e);
            return new Result<>(500, "恢复失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取待补客资列表
     */
    @GetMapping("/compensation/list")
    public Result<List<ConsultantCompensation>> getPendingCompensations() {
        try {
            List<ConsultantCompensation> compensations = consultantsService.getPendingCompensations();
            return new Result<>(200, "获取成功", compensations);
        } catch (Exception e) {
            log.error("获取待补客资列表失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }

    /**
     * 更新顾问状态（暂停/休息）
     */
    @PostMapping("/status")
    @Log(value = "更新顾问状态", module = "顾问管理")
    public Result<Void> updateConsultantStatus(@RequestParam Integer consultantId,
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

            consultantsService.updateStatus(consultantId, status);

            Consultants updatedConsultant = consultantsMapper.selectById(consultantId);
            String consultantJson = new ObjectMapper().writeValueAsString(updatedConsultant);
            myWebSocketHandler.sendToAllClients(consultantJson);
            return new Result<>(200, "状态更新成功", null);


        } catch (Exception e) {
            log.error("更新状态失败:", e);
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }

    /**
     * 标记补偿完成
     */
    @PostMapping("/compensation/complete")
    @Log(value = "完成补客资", module = "顾问管理")
    public Result<Void> completeCompensation(@RequestBody Map<String, String> params) {
        try {
            String compensationId = params.get("compensationId");
            log.info("收到的补偿ID: {}", compensationId);

            ConsultantCompensation compensation = compensationMapper.selectById(compensationId);
            if (compensation == null) {
                log.error("未找到ID为{}的补偿记录", compensationId);
                return new Result<>(404, "补偿记录不存在", null);
            }

            compensation.setStatus(2);
            compensationMapper.updateById(compensation);

            return new Result<>(200, "标记完成成功", null);
        } catch (Exception e) {
            log.error("标记补偿完成失败:", e);
            return new Result<>(500, "操作失败: " + e.getMessage(), null);
        }
    }

    @PostMapping("/clear-normal-count")
    @Log(value = "清零顾问官号客资", module = "顾问管理")
    public Result<Void> clearNormalCount(@RequestBody Map<String, Integer> params) {
        try {
            Integer type = params.get("type");
            if (type == null || (type != 1 && type != 2)) {
                return new Result<>(400, "无效的顾问类型", null);
            }

            consultantsService.clearNormalCount(type);
            return new Result<>(200, "清零成功", null);
        } catch (Exception e) {
            log.error("清零失败:", e);
            return new Result<>(500, "清零失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取顾问账号类型关联
     */
    @GetMapping("/consultant-account-types/{consultantId}")
    public Result<Map<String, Object>> getConsultantAccountTypes(@PathVariable Integer consultantId) {
        try {
            Consultants consultant = consultantsMapper.selectById(consultantId);
            if (consultant == null) {
                return new Result<>(404, "顾问不存在", null);
            }
            
            // 获取顾问关联的账号类型
            List<ConsultantAccountRelation> relations = consultantAccountRelationMapper.selectByConsultantId(consultantId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("consultant", consultant);
            result.put("accountRelations", relations);
            
            return new Result<>(200, "获取成功", result);
        } catch (Exception e) {
            log.error("获取顾问账号类型关联失败:", e);
            return new Result<>(500, "获取失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 更新顾问与账号类型的关联状态 (客资数、排序状态、等待数)
     */
    @PostMapping("/update-account-relation")
    @Log(value = "更新顾问账号关联状态", module = "顾问管理")
    public Result<Void> updateConsultantAccountRelation(
            @RequestBody ConsultantAccountRelation relation,
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
            
            // 查询是否存在关联
            ConsultantAccountRelation existingRelation = consultantAccountRelationMapper.selectByConsultantAndAccountType(
                    relation.getConsultantId(), relation.getAccountTypeId());
            
            if (existingRelation == null) {
                // 不存在则创建
                consultantAccountRelationMapper.insert(relation);
            } else {
                // 更新现有关联
                relation.setId(existingRelation.getId());
                consultantAccountRelationMapper.updateById(relation);
            }
            
            // 获取更新后的顾问信息并通过WebSocket广播
            Consultants consultant = consultantsMapper.selectById(relation.getConsultantId());
            String consultantJson = new ObjectMapper().writeValueAsString(consultant);
            myWebSocketHandler.sendToAllClients(consultantJson);
            
            return new Result<>(200, "更新成功", null);
        } catch (Exception e) {
            log.error("更新顾问账号关联状态失败:", e);
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 更新顾问排序顺序
     * 处理拖拽排序后的顾问列表
     */
    @PostMapping("/consultants/reorder")
    @Log(value = "更新顾问排序", module = "顾问管理")
    @Transactional
    public Result<Void> reorderConsultants(@RequestBody Map<String, List<Map<String, Object>>> requestBody,
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
            
            List<Map<String, Object>> updates = requestBody.get("updates");
            if (updates == null || updates.isEmpty()) {
                return new Result<>(400, "无效的请求数据", null);
            }
            
            log.info("收到顾问排序更新请求: {}", updates);
            
            // 批量更新顾问排序
            for (Map<String, Object> update : updates) {
                Integer id = ((Number) update.get("id")).intValue();
                Integer newOrder = ((Number) update.get("newOrder")).intValue();
                Integer type = ((Number) update.get("type")).intValue();
                
                // 使用服务层方法更新排序
                boolean success = consultantsService.updateConsultantSortOrder(id, newOrder);
                if (!success) {
                    log.warn("更新顾问ID:{}的排序失败", id);
                } else {
                    log.info("已更新顾问ID:{}的排序为:{}", id, newOrder);
                    
                    // 对每个更新成功的顾问单独发送WebSocket通知
                    Consultants updatedConsultant = consultantsService.getConsultantById(id);
                    if (updatedConsultant != null) {
                        String consultantJson = new ObjectMapper().writeValueAsString(updatedConsultant);
                        myWebSocketHandler.sendToAllClients(consultantJson);
                    }
                }
            }
            
            return new Result<>(200, "顾问排序更新成功", null);
        } catch (Exception e) {
            log.error("更新顾问排序失败:", e);
            return new Result<>(500, "更新失败: " + e.getMessage(), null);
        }
    }

    @Transactional
    public void initializeSortOrder() {
        List<Consultants> allConsultants = consultantsMapper.selectList(null);

        // 按类型分组
        Map<Integer, List<Consultants>> groupedByType = allConsultants.stream()
                .collect(Collectors.groupingBy(Consultants::getType));

        // 为每种类型的顾问设置排序顺序
        for (Map.Entry<Integer, List<Consultants>> entry : groupedByType.entrySet()) {
            Integer type = entry.getKey();
            List<Consultants> consultantsOfType = entry.getValue();

            // 按ID排序
            consultantsOfType.sort(Comparator.comparing(Consultants::getId));

            // 设置排序字段
            for (int i = 0; i < consultantsOfType.size(); i++) {
                Consultants consultant = consultantsOfType.get(i);
                consultant.setSortOrder(i + 1);
                consultantsMapper.updateById(consultant);
            }
        }
    }
}
