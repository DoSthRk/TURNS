package com.qj.back.service;
import com.qj.back.Handler.Result;
import com.qj.back.entity.Schedule;
import com.qj.back.entity.ShiftType;

import java.util.List;
public interface ScheduleService {
    Result<List<Schedule>> getScheduleList();

    // 获取班次类型列表
    Result<List<ShiftType>> getShiftTypes();

    // 添加排班
    Result<String> addSchedule(Schedule schedule, String account);

    // 删除排班
    Result<String> deleteSchedule(Integer id, String account);

    Result<String> batchAddSchedule(List<Schedule> schedules, String account);
    Result<Schedule> getTodaySchedule(String account);
}
