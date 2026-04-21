package com.qj.back.service;

import com.qj.back.entity.Schedule;

public interface ScheduleTodoService {
    void initTodosForSchedule(Schedule schedule);
    Schedule getTodayScheduleForUser(Integer userId);
}
