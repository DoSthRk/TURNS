package com.qj.back.controller;

import com.qj.back.Handler.Result;
import com.qj.back.entity.ConsultantCompensation;
import com.qj.back.service.CompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/compensation")
@SuppressWarnings("all")
@Slf4j
public class CompensationController {
    @Autowired
    private CompensationService compensationService;

    @PostMapping("/statistics")
    public Result<Void> saveCompensationStatistics(@RequestBody List<ConsultantCompensation> compensationData) {
        try {
            log.info("接收到的统计数据: {}", compensationData);
            compensationService.saveCompensationStatistics(compensationData);
            return new Result<>(200, "统计数据保存成功", null);
        } catch (Exception e) {
            log.error("保存统计数据失败:", e);
            return new Result<>(500, "保存失败: " + e.getMessage(), null);
        }
    }

    @PostMapping("/delete")
    public Result<Void> deleteCompensation(@RequestParam Integer compensationId) {
        compensationService.deleteCompensation(compensationId);
        return new Result<>(200, "删除成功", null);
    }
    @PostMapping("/clear-all")
    public Result<Void> clearAllCompensation() {
        try {
            log.info("开始清除所有统计数据");
            compensationService.clearAllCompensation();
            return new Result<>(200, "所有统计数据已清除", null);
        } catch (Exception e) {
            log.error("清除统计数据失败:", e);
            return new Result<>(500, "清除失败: " + e.getMessage(), null);
        }
    }
}
