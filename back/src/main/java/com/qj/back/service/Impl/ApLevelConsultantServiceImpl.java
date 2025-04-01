package com.qj.back.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qj.back.entity.ApLevelConsultant;
import com.qj.back.mapper.ApLevelConsultantMapper;
import com.qj.back.service.ApLevelConsultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApLevelConsultantServiceImpl implements ApLevelConsultantService {
    @Autowired
    private ApLevelConsultantMapper apLevelConsultantMapper;

    @Override
    public List<ApLevelConsultant> getActiveConsultants() {
        return apLevelConsultantMapper.selectList(
                new QueryWrapper<ApLevelConsultant>()
                        .eq("status", 1)
                        .orderByAsc("count_single1")
        );
    }

    @Override
    public boolean updateCount(Long consultantId, Integer countSingle1) {
        ApLevelConsultant consultant = new ApLevelConsultant();
        consultant.setId(consultantId);
        consultant.setCountSingle1(countSingle1);
        return apLevelConsultantMapper.updateById(consultant) > 0;
    }

    @Override
    public boolean updateStatus(Long consultantId, Integer status) {
        ApLevelConsultant consultant = new ApLevelConsultant();
        consultant.setId(consultantId);
        consultant.setStatus(status);
        return apLevelConsultantMapper.updateById(consultant) > 0;
    }

    @Override
    public boolean updateWaitingCount(Long consultantId, Integer waitingSingle1) {
        ApLevelConsultant consultant = new ApLevelConsultant();
        consultant.setId(consultantId);
        consultant.setWaitingSingle1(waitingSingle1);
        return apLevelConsultantMapper.updateById(consultant) > 0;
    }

    @Override
    public boolean updateOrderStatus(Long consultantId, Integer orderStatusSingle1) {
        ApLevelConsultant consultant = new ApLevelConsultant();
        consultant.setId(consultantId);
        consultant.setOrderStatusSingle1(orderStatusSingle1);
        return apLevelConsultantMapper.updateById(consultant) > 0;
    }

}
