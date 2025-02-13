package com.qj.back.controller;

import com.qj.back.entity.User;
import com.qj.back.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.qj.back.Handler.Result;
import com.qj.back.service.GameScoreService;
import com.qj.back.utils.JwtUtil;
import com.qj.back.vo.GameScoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/game")
@CrossOrigin
public class GameController {
    @Autowired
    private GameScoreService gameScoreService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/score")
    public Result<Void> submitScore(@RequestBody Map<String, Object> params,
                                    @RequestHeader("Authorization") String token) {
        log.info("接收到分数提交请求 - params: {}, token: {}", params, token);

        try {
            // 处理 Bearer token
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                log.info("处理后的token: {}", token);
            } else {
                log.warn("token格式不正确");
                return new Result<>(401, "未登录", null);
            }

            // 从token中获取用户信息
            String account = JwtUtil.getAccountFromToken(token);
            log.info("从token中获取的账号: {}", account);

            if (account == null) {
                log.warn("无法从token获取账号");
                return new Result<>(401, "未登录", null);
            }

            // 获取用户ID
            User user = userMapper.findByAccount(account);
            log.info("查询到的用户信息: {}", user);

            if (user == null) {
                log.warn("未找到用户信息");
                return new Result<>(500, "用户不存在", null);
            }

            String gameType = (String) params.get("type");
            Integer score = (Integer) params.get("score");
            log.info("准备保存分数 - userId: {}, gameType: {}, score: {}",
                    user.getId(), gameType, score);

            gameScoreService.saveScore(user.getId(), gameType, score);
            return new Result<>(200, "提交成功", null);
        } catch (Exception e) {
            log.error("保存分数失败", e);
            return new Result<>(500, e.getMessage(), null);
        }
    }
    @GetMapping("/leaderboard/{type}")
    public Result<List<GameScoreVO>> getLeaderboard(@PathVariable String type,
                                                    @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("获取排行榜 - type: {}, token: {}", type, token);  // 添加日志

        // 处理 Bearer token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (token == null || !JwtUtil.validateToken(token)) {
            log.warn("token验证失败");  // 添加日志
            return new Result<>(401, "未登录", null);
        }

        try {
            List<GameScoreVO> leaderboard = gameScoreService.getLeaderboard(type);
            return new Result<>(200, "获取成功", leaderboard);
        } catch (Exception e) {
            log.error("获取排行榜失败", e);
            return new Result<>(500, e.getMessage(), null);
        }
    }

    @GetMapping("/best-scores")
    public Result<Map<String, Integer>> getUserBestScores(@RequestHeader("Authorization") String token) {
        String account = JwtUtil.getAccountFromToken(token);
        if (account == null) {
            return new Result<>(401, "未登录", null);
        }

        try {
            User user = userMapper.findByAccount(account);
            if (user == null) {
                return new Result<>(401, "用户不存在", null);
            }

            Map<String, Integer> bestScores = gameScoreService.getUserAllBestScores(user.getId());
            return new Result<>(200, "获取成功", bestScores);
        } catch (Exception e) {
            log.error("获取用户最高分失败", e);
            return new Result<>(500, e.getMessage(), null);
        }
    }
}
