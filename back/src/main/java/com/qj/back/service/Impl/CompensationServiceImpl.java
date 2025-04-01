package com.qj.back.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qj.back.entity.ConsultantCompensation;
import com.qj.back.mapper.ConsultantCompensationMapper;
import com.qj.back.service.CompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Service
@Slf4j
public class CompensationServiceImpl implements CompensationService {
    @Autowired
    private ConsultantCompensationMapper compensationMapper;

    @Override
    public void saveCompensationStatistics(List<ConsultantCompensation> compensationData) {
        for (ConsultantCompensation compensation : compensationData) {
            compensationMapper.insertCompensationStatistics(
                    compensation.getConsultantId(),
                    compensation.getType(),
                    compensation.getNormalCount(),
                    new Date(), // 当前时间
                    1,
                    compensation.getDifference()// 状态设为1，表示正常
            );
        }
    }

    @Override
    public void deleteCompensation(Integer compensationId) {
        compensationMapper.deleteById(compensationId); // 使用 MyBatis-Plus 的 deleteById 方法
    }

    @Override
    public void clearAllCompensation() {
        log.info("执行清除所有统计数据");
        // 使用MyBatis-Plus的delete方法，传入null作为条件即可删除所有数据
        compensationMapper.delete(null);
        log.info("统计数据清除完成");
    }

}
