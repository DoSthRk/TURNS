package com.qj.back.service;

import com.qj.back.entity.ShiftTypeConfig;

import java.util.List;

public interface ShiftTypeConfigService {
    List<ShiftTypeConfig> getAllTypeConfigs();

    List<ShiftTypeConfig> getEnabledTypeConfigs();

    boolean saveTypeConfigs(List<ShiftTypeConfig> configs);
}

