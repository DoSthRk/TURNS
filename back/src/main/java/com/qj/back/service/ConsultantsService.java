package com.qj.back.service;

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
}
