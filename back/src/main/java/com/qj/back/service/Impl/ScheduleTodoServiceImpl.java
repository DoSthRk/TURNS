package com.qj.back.service.Impl;

import com.qj.back.entity.Schedule;
import com.qj.back.entity.TodoType;
import com.qj.back.entity.UserTodo;
import com.qj.back.mapper.ScheduleMapper;
import com.qj.back.mapper.ShiftTypeMapper;
import com.qj.back.mapper.TodoTypeMapper;
import com.qj.back.mapper.UserTodoMapper;
import com.qj.back.service.ScheduleTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ScheduleTodoServiceImpl implements ScheduleTodoService {
    @Autowired
    private UserTodoMapper userTodoMapper;

    @Autowired
    private TodoTypeMapper todoTypeMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ShiftTypeMapper shiftTypeMapper;

    @Override
    public void initTodosForSchedule(Schedule schedule) {
        // 获取班次对应的待办事项类型
        List<TodoType> todoTypes = todoTypeMapper.selectByShiftTypeId(schedule.getShiftTypeId());

        // 为每个待办事项类型创建用户待办事项
        todoTypes.forEach(todoType -> {
            UserTodo userTodo = new UserTodo();
            userTodo.setUserId(schedule.getUserId());
            userTodo.setTodoTypeId(todoType.getId());
            userTodo.setScheduleId(schedule.getId());
            userTodo.setIsCompleted(false);

            userTodoMapper.insert(userTodo);
        });
    }

    @Override
    public Schedule getTodayScheduleForUser(Integer userId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Schedule schedule = scheduleMapper.selectTodaySchedule(userId, today);

        if (schedule != null) {
            schedule.setShiftType(shiftTypeMapper.selectById(schedule.getShiftTypeId()));
        }

        return schedule;
    }
}
