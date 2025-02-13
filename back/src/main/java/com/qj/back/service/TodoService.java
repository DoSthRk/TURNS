package com.qj.back.service;

import com.qj.back.Handler.Result;
import com.qj.back.entity.Schedule;
import com.qj.back.entity.UserTodo;

import java.util.List;

public interface TodoService {
    Result<List<UserTodo>> getTodoList(String account);
    Result<String> completeTodo(Integer todoId, String account);
    void initTodosForSchedule(Schedule schedule);
}
