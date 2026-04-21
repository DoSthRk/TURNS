package com.qj.back.service;

import com.qj.back.entity.ConsultantCompensation;

import java.util.List;

public interface CompensationService {
    void saveCompensationStatistics(List<ConsultantCompensation> compensationData);
    void deleteCompensation(Integer compensationId);
    void clearAllCompensation();


}
