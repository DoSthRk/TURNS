package com.qj.back.service;

import com.qj.back.entity.ShiftModuleConfig;

import java.util.List;
import java.util.Map;

public interface ShiftModuleConfigService {
    Map<Integer, List<ShiftModuleConfig>> getAllByType();

    List<ShiftModuleConfig> getByConsultantType(Integer consultantType);

    boolean saveByConsultantType(Integer consultantType, List<ShiftModuleConfig> configs);
}

