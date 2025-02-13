package com.qj.back.service;

import com.qj.back.Handler.Result;
import com.qj.back.entity.Advice;

import java.util.List;

public interface AdviceService {
    Result<String> addAdvice(Advice advice, String account);
    Result<List<Advice>> getAllAdvice();
    Result<String> deleteAdvice(Integer id);
    Result<List<Advice>> getAdviceByAccount(String account);  // 新增方法

}
