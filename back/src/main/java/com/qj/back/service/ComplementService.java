package com.qj.back.service;

import com.qj.back.entity.Complement;

import java.util.List;

public interface ComplementService {
    List<Complement> findAll();
    boolean add(Complement complement);
    boolean update(Complement complement);
    List<Complement> findByType(Integer type);
    boolean delete(Long complementId);  // 新增此方法
    Complement getById(Long complementId);
    boolean deleteByType(Integer type);

}
