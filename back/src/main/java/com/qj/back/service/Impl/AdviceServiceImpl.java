package com.qj.back.service.Impl;

import com.qj.back.Handler.Result;
import com.qj.back.entity.Advice;
import com.qj.back.entity.User;
import com.qj.back.mapper.AdviceMapper;
import com.qj.back.service.AdviceService;
import com.qj.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdviceServiceImpl implements AdviceService {

    private final AdviceMapper adviceMapper;
    private final UserService userService;

    @Override
    public Result<String> addAdvice(Advice advice, String account) {
        try {
            // 获取用户信息
            User user = userService.getUserByAccount(account);
            if (user == null) {
                return new Result<>(400, "用户不存在", null);
            }

            // 设置留言信息
            advice.setUserId(user.getId());
            advice.setCreateTime(new Date());

            // 保存留言
            adviceMapper.insert(advice);
            return new Result<>(200, "留言成功", null);
        } catch (Exception e) {
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @Override
    public Result<List<Advice>> getAllAdvice() {
        try {
            // 获取所有留言
            List<Advice> adviceList = adviceMapper.selectList();

            // 填充用户信息
            for (Advice advice : adviceList) {
                User user = userService.getUserById(advice.getUserId());
                if (user != null) {
                    advice.setUser(user);
                }
            }

            return new Result<>(200, "获取成功", adviceList);
        } catch (Exception e) {
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @Override
    public Result<String> deleteAdvice(Integer id) {
        try {
            adviceMapper.deleteById(id);
            return new Result<>(200, "删除成功", null);
        } catch (Exception e) {
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @Override
    public Result<List<Advice>> getAdviceByAccount(String account) {
        try {
            User user = userService.getUserByAccount(account);
            if (user == null) {
                return new Result<>(400, "用户不存在", null);
            }

            List<Advice> adviceList = adviceMapper.selectByAccount(user.getAccount());
            // 填充用户信息
            for (Advice advice : adviceList) {
                advice.setUser(user);
            }

            return new Result<>(200, "获取成功", adviceList);
        } catch (Exception e) {
            return new Result<>(500, "服务器错误: " + e.getMessage(), null);
        }
    }
}
