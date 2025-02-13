package com.qj.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.qj.back.Handler.MyWebSocketHandler;
import com.qj.back.Handler.Result;
import com.qj.back.entity.Consultants;
import com.qj.back.annotation.Log;
import com.qj.back.mapper.ConsultantsMapper;
import com.qj.back.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@CrossOrigin
@SuppressWarnings("all")
@Slf4j
public class ConsultantsController {

    @Autowired
    private ConsultantsMapper consultantsMapper;

    @Autowired
    private MyWebSocketHandler myWebSocketHandler;
    private Gson gson = new Gson();

    @GetMapping("/consultants")
    public String getConsultants() {
        List<Consultants> consultants = consultantsMapper.selectList(null);
        System.out.println(gson.toJson(consultants));
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
    public void addConsultants(@RequestBody Consultants consultants) {
        consultantsMapper.insert(consultants);
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



}
