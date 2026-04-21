package com.qj.back.service;

import com.qj.back.entity.ApLevelConsultant;

import java.util.List;

public interface ApLevelConsultantService {
    /**
     * 获取所有状态正常的AP/Alevel顾问
     */
    List<ApLevelConsultant> getActiveConsultants();

    /**
     * 更新顾问的AP/Alevel客资数
     */
    boolean updateCount(Long consultantId, Integer countSingle1);

    /**
     * 更新顾问状态
     */
    boolean updateStatus(Long consultantId, Integer status);

    /**
     * 更新待分配客资数
     */
    boolean updateWaitingCount(Long consultantId, Integer waitingSingle1);

    /**
     * 更新轮班状态
     */
    boolean updateOrderStatus(Long consultantId, Integer orderStatusSingle1);
}
