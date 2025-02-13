package com.qj.back.service.Impl;

import com.qj.back.Handler.Result;
import com.qj.back.entity.Schedule;
import com.qj.back.entity.TodoType;
import com.qj.back.entity.User;
import com.qj.back.entity.UserTodo;
import com.qj.back.mapper.TodoTypeMapper;
import com.qj.back.mapper.UserTodoMapper;
import com.qj.back.service.ScheduleService;
import com.qj.back.service.ScheduleTodoService;
import com.qj.back.service.TodoService;
import com.qj.back.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Slf4j
@Service
@Transactional
public class TodoServiceImpl implements TodoService {
    @Autowired
    private UserTodoMapper userTodoMapper;

    @Autowired
    private TodoTypeMapper todoTypeMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ScheduleTodoService scheduleTodoService;  // 替换原来的 ScheduleService

    @Override
    public Result<List<UserTodo>> getTodoList(String account) {
        try {
            log.info("开始获取待办事项，用户账号: {}", account);

            User user = userService.getUserByAccount(account);
            if (user == null) {
                log.error("用户不存在: {}", account);
                return new Result<>(404, "用户不存在", null);
            }

            Schedule schedule = scheduleTodoService.getTodayScheduleForUser(user.getId());
            if (schedule == null) {
                log.info("用户今日无排班: {}", account);
                return new Result<>(200, "今日无排班", new ArrayList<>());
            }
            log.info("用户 {} 今日排班ID: {}, 班次ID: {}", account, schedule.getId(), schedule.getShiftTypeId());

            // 查询待办事项
            List<UserTodo> todos = userTodoMapper.selectByUserAndSchedule(user.getId(), schedule.getId());
            log.info("查询到 {} 条待办事项", todos.size());

            // 如果没有待办事项，则初始化
            if (todos.isEmpty()) {
                log.info("开始初始化待办事项，班次ID: {}", schedule.getShiftTypeId());
                List<TodoType> todoTypes = todoTypeMapper.selectByShiftTypeId(schedule.getShiftTypeId());
                log.info("找到 {} 个待办事项类型", todoTypes.size());

                if (todoTypes.isEmpty()) {
                    log.warn("该班次没有预设的待办事项类型！");
                    return new Result<>(200, "该班次无待办事项", new ArrayList<>());
                }

                for (TodoType todoType : todoTypes) {
                    UserTodo userTodo = new UserTodo();
                    userTodo.setUserId(user.getId());
                    userTodo.setTodoTypeId(todoType.getId());
                    userTodo.setScheduleId(schedule.getId());
                    userTodo.setIsCompleted(false);

                    int result = userTodoMapper.insert(userTodo);
                    log.info("创建待办事项: {}, 结果: {}", todoType.getContent(), result);
                }

                // 重新查询待办事项
                todos = userTodoMapper.selectByUserAndSchedule(user.getId(), schedule.getId());
                log.info("初始化后查询到 {} 条待办事项", todos.size());
            }

            // 确保每个待办事项都有类型信息
            todos.forEach(todo -> {
                if (todo.getTodoType() == null) {
                    TodoType todoType = todoTypeMapper.selectById(todo.getTodoTypeId());
                    todo.setTodoType(todoType);
                    log.info("待办事项内容: {}", todoType.getContent());
                }
            });

            return new Result<>(200, "获取成功", todos);
        } catch (Exception e) {
            log.error("获取待办事项失败:", e);
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }
    @Override
    public Result<String> completeTodo(Integer todoId, String account) {
        try {
            User user = userService.getUserByAccount(account);
            if (user == null) {
                return new Result<>(404, "用户不存在", null);
            }

            UserTodo todo = new UserTodo();
            todo.setId(todoId);
            todo.setIsCompleted(true);
            todo.setCompleteTime(new Date());

            userTodoMapper.updateStatus(todo);
            return new Result<>(200, "更新成功", null);
        } catch (Exception e) {
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }

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
}
