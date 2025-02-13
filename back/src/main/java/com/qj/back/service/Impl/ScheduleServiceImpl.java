package com.qj.back.service.Impl;

import com.qj.back.Handler.Result;
import com.qj.back.entity.Schedule;
import com.qj.back.entity.ShiftType;
import com.qj.back.entity.User;
import com.qj.back.mapper.ScheduleMapper;
import com.qj.back.mapper.ShiftTypeMapper;
import com.qj.back.mapper.UserTodoMapper;
import com.qj.back.service.ScheduleService;
import com.qj.back.service.ScheduleTodoService;
import com.qj.back.service.TodoService;
import com.qj.back.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("all")
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleTodoService scheduleTodoService;  // 替换原来的 TodoService



    private final ScheduleMapper scheduleMapper;
    private final ShiftTypeMapper shiftTypeMapper;
    private final UserService userService;
    @Autowired
    private UserTodoMapper userTodoMapper;

    private boolean isAdmin(User user) {
        return user != null && "admin".equals(user.getRole());
    }

    @Override
    public Result<List<Schedule>> getScheduleList() {
        try {
            List<Schedule> schedules = scheduleMapper.selectList();
            return new Result<>(200, "获取成功", schedules);
        } catch (Exception e) {
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @Override
    public Result<List<ShiftType>> getShiftTypes() {
        try {
            List<ShiftType> types = shiftTypeMapper.selectList();
            return new Result<>(200, "获取成功", types);
        } catch (Exception e) {
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @Override
    public Result<String> addSchedule(Schedule schedule, String account) {
        try {
            // 验证管理员权限
            User user = userService.getUserByAccount(account);
            if (!isAdmin(user)) {
                return new Result<>(403, "无权限", null);
            }

            // 检查是否已经排班
            int count = scheduleMapper.countByDateAndUser(schedule.getDate(), schedule.getUserId());
            if (count > 0) {
                return new Result<>(400, "该用户在这一天已经有排班", null);
            }

            // 设置创建信息
            schedule.setCreatedBy(user.getId());
            schedule.setCreateTime(new Date());

            // 添加排班
            scheduleMapper.insert(schedule);

            // 初始化待办事项 - 确保这行代码被执行
            scheduleTodoService.initTodosForSchedule(schedule);

            return new Result<>(200, "添加成功", null);
        } catch (Exception e) {
            log.error("添加排班失败:", e);
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @Override
    public Result<String> deleteSchedule(Integer id, String account) {
        try {
            // 验证权限
            User user = userService.getUserByAccount(account);
            if (!"admin".equals(user.getRole())) {
                return new Result<>(403, "无权限", null);
            }

            // 先查询排班是否存在
            Schedule schedule = scheduleMapper.selectById(id);
            if (schedule == null) {
                return new Result<>(404, "排班不存在", null);
            }

            log.info("开始删除排班，ID: {}", id);

            // 1. 先删除关联的待办事项
            int todoCount = userTodoMapper.deleteByScheduleId(id);
            log.info("已删除关联的待办事项数量: {}", todoCount);

            // 2. 再删除排班记录
            int result = scheduleMapper.deleteById(id);
            log.info("删除排班结果: {}", result > 0 ? "成功" : "失败");

            if (result > 0) {
                return new Result<>(200, "删除成功", null);
            } else {
                return new Result<>(500, "删除失败", null);
            }
        } catch (Exception e) {
            log.error("删除排班失败:", e);
            // 由于添加了 @Transactional，发生异常时会自动回滚
            throw new RuntimeException("删除排班失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> batchAddSchedule(List<Schedule> schedules, String account) {
        try {
            // 验证管理员权限
            User admin = userService.getUserByAccount(account);
            if (!"admin".equals(admin.getRole())) {
                return new Result<>(403, "无权限进行排班操作", null);
            }

            // 验证数据
            if (schedules == null || schedules.isEmpty()) {
                return new Result<>(400, "排班数据不能为空", null);
            }

            // 检查日期冲突
            List<String> conflicts = new ArrayList<>();
            for (Schedule schedule : schedules) {
                int count = scheduleMapper.countByDateAndUser(schedule.getDate(), schedule.getUserId());
                if (count > 0) {
                    User user = userService.getUserById(schedule.getUserId());
                    conflicts.add(String.format("%s 在 %s 已有排班",
                            user.getAccount(), schedule.getDate()));
                }
            }

            // 如果有冲突，返回错误信息
            if (!conflicts.isEmpty()) {
                return new Result<>(400, "存在排班冲突：" + String.join(", ", conflicts), null);
            }

            // 批量插入排班记录
            Date now = new Date();
            for (Schedule schedule : schedules) {
                schedule.setCreatedBy(admin.getId());
                schedule.setCreateTime(now);
                scheduleMapper.insert(schedule);
            }

            return new Result<>(200, "批量排班成功", null);
        } catch (Exception e) {
            // 发生异常时回滚事务
            throw new RuntimeException("批量排班失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Schedule> getTodaySchedule(String account) {
        try {
            User user = userService.getUserByAccount(account);
            if (user == null) {
                return new Result<>(404, "用户不存在", null);
            }

            Schedule schedule = scheduleTodoService.getTodayScheduleForUser(user.getId());
            if (schedule != null) {
                schedule.setUser(user);
            }

            return new Result<>(200, "获取成功", schedule);
        } catch (Exception e) {
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }


}
