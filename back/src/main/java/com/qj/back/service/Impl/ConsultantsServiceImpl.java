package com.qj.back.service.Impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qj.back.entity.ConsultantCompensation;
import com.qj.back.entity.Consultants;
import com.qj.back.mapper.ConsultantCompensationMapper;
import com.qj.back.mapper.ConsultantsMapper;
import com.qj.back.service.ConsultantsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class ConsultantsServiceImpl implements ConsultantsService {
    // 实现所有接口方法
    // 把原来 ConsultantsService 类中的具体实现代码复制到这里
    @Autowired
    private ConsultantsMapper consultantsMapper;

    @Autowired
    private ConsultantCompensationMapper compensationMapper;

    @Override
    public void updateStatus(Integer id, Integer status) {
        if (status == 3) {
            int pendingCount = compensationMapper.countPendingByConsultantId(id);
            if (pendingCount > 0) {
                throw new RuntimeException("该顾问还有未完成的待补客资记录");
            }
        }
        consultantsMapper.updateWorkStatus(id, status);  // 使用新方法
    }

    @Override
    public void updateShiftOrder(Integer id, Integer orderStatus) {
        consultantsMapper.updateShiftOrderById(id, orderStatus);
    }

    @Override
    public List<Consultants> getConsultantsOrderedByNormal() {
        return consultantsMapper.selectConsultantsOrderedByNormal();
    }

    @Override
    public List<Consultants> getConsultantsOrderedBySem() {
        return consultantsMapper.selectConsultantsOrderedBySem();
    }

    @Override
    public List<Consultants> getConsultantsOrderedBySingle1() {
        return consultantsMapper.selectConsultantsOrderedBySingle1();
    }

    @Override
    public List<Consultants> getAllConsultants() {
        return consultantsMapper.selectList(null);
    }

    @Override
    public Consultants getConsultantById(Integer id) {
        return consultantsMapper.selectById(id);
    }

    @Override
    public void restoreFromPause(Integer consultantId) {
        Consultants consultant = consultantsMapper.selectById(consultantId);
        if (consultant == null || consultant.getStatus() != 2) {
            throw new RuntimeException("顾问不存在或状态不是暂停");
        }

        // 只更新顾问状态
        consultant.setStatus(1);
        consultantsMapper.updateById(consultant);

    }

    @Override
    public void restoreFromBreak(Integer consultantId) {
        Consultants consultant = consultantsMapper.selectById(consultantId);
        if (consultant == null || consultant.getStatus() != 3) {
            throw new RuntimeException("顾问不存在或状态不是休息");
        }

        // 只更新顾问状态
        consultant.setStatus(1);
        consultantsMapper.updateById(consultant);

        // 取消统计待补客资的逻辑
        // 不再创建补偿记录
    }

    @Override
    public List<ConsultantCompensation> getPendingCompensations() {
        return compensationMapper.selectPendingCompensations();
    }

    @Override
    @Transactional
    public void clearNormalCount(Integer type) {
        // 更新指定类型且状态为1的顾问的 count_normal 为 0
        consultantsMapper.clearNormalCount(type);
    }

    @Override
    public List<Consultants> getConsultantsByTypeOrderedBySortOrder(Integer type) {
        return consultantsMapper.selectConsultantsByTypeOrderedBySortOrder(type);
    }

    @Override
    @Transactional
    public boolean updateConsultantSortOrder(Integer id, Integer newOrder) {
        try {
            Consultants consultant = consultantsMapper.selectById(id);
            if (consultant == null) {
                log.warn("未找到ID为{}的顾问", id);
                return false;
            }
            
            consultant.setSortOrder(newOrder);
            int result = consultantsMapper.updateById(consultant);
            return result > 0;
        } catch (Exception e) {
            log.error("更新顾问排序失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
