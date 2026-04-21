package com.qj.back.service;

import com.qj.back.entity.ConsultantCompensation;
import com.qj.back.entity.Consultants;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConsultantsService {
    void updateStatus(Integer id, Integer status);
    void updateShiftOrder(Integer id, Integer orderStatus);
    List<Consultants> getConsultantsOrderedByNormal();
    List<Consultants> getConsultantsOrderedBySem();
    List<Consultants> getConsultantsOrderedBySingle1();
    List<Consultants> getAllConsultants();
    Consultants getConsultantById(Integer id);
    void restoreFromPause(Integer consultantId);
    void restoreFromBreak(Integer consultantId);
    List<ConsultantCompensation> getPendingCompensations();
    /**
     * 清零指定类型且状态正常的顾问的官号客资数
     * @param type 顾问类型（1：辅导，2：申诉）
     */
    void clearNormalCount(Integer type);

    /**
     * 获取按排序顺序排列的指定类型顾问列表
     * @param type 顾问类型（1：辅导，2：申诉，3：单聊1）
     * @return 排序后的顾问列表
     */
    List<Consultants> getConsultantsByTypeOrderedBySortOrder(Integer type);

    /**
     * 更新顾问排序顺序
     * @param id 顾问ID
     * @param newOrder 新的排序顺序
     * @return 更新是否成功
     */
    boolean updateConsultantSortOrder(Integer id, Integer newOrder);
}
