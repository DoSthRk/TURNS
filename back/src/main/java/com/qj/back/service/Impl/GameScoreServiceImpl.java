package com.qj.back.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qj.back.entity.GameScore;
import com.qj.back.entity.User;
import com.qj.back.mapper.GameScoreMapper;
import com.qj.back.mapper.UserMapper;
import com.qj.back.service.GameScoreService;
import com.qj.back.vo.GameScoreVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GameScoreServiceImpl implements GameScoreService {

    @Autowired
    private GameScoreMapper gameScoreMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void saveScore(Integer userId, String gameType, Integer score) {
        log.info("保存游戏分数 - userId: {}, gameType: {}, score: {}",
                userId, gameType, score);  // 添加日志
        // 参数校验
        if (userId == null || !StringUtils.hasText(gameType) || score == null) {
            log.error("保存游戏分数失败：参数为空 - userId: {}, gameType: {}, score: {}", userId, gameType, score);
            throw new RuntimeException("参数不能为空");
        }

        // 验证游戏类型
        if (!Arrays.asList("2048", "snake", "memory").contains(gameType)) {
            log.error("保存游戏分数失败：无效的游戏类型 - {}", gameType);
            throw new RuntimeException("无效的游戏类型");
        }

        try {
            // 获取用户当前最高分
            Integer bestScore = getUserBestScore(userId, gameType);

            // 只有超过最高分时才保存
            if (score > bestScore) {
                GameScore gameScore = new GameScore();
                gameScore.setUserId(userId);
                gameScore.setGameType(gameType);
                gameScore.setScore(score);
                gameScore.setCreateTime(LocalDateTime.now());

                gameScoreMapper.insert(gameScore);
                log.info("保存新的最高分成功 - userId: {}, gameType: {}, score: {}", userId, gameType, score);
            } else {
                log.info("分数未超过最高分，不保存 - userId: {}, gameType: {}, currentScore: {}, bestScore: {}",
                        userId, gameType, score, bestScore);
            }
        } catch (Exception e) {
            log.error("保存游戏分数失败", e);
            throw new RuntimeException("保存分数失败");
        }
    }

    @Override
    public List<GameScoreVO> getLeaderboard(String gameType) {
        if (!StringUtils.hasText(gameType)) {
            log.error("获取排行榜失败：游戏类型为空");
            throw new RuntimeException("游戏类型不能为空");
        }

        try {
            // 获取前10名
            LambdaQueryWrapper<GameScore> wrapper = Wrappers.<GameScore>lambdaQuery()
                    .eq(GameScore::getGameType, gameType)
                    .orderByDesc(GameScore::getScore)
                    .last("LIMIT 10");

            List<GameScore> scores = gameScoreMapper.selectList(wrapper);

            if (scores == null || scores.isEmpty()) {
                return new ArrayList<>();
            }

            // 获取所有相关用户ID
            Set<Integer> userIds = scores.stream()
                    .map(GameScore::getUserId)
                    .collect(Collectors.toSet());

            // 批量查询用户信息
            List<User> users = userIds.stream()
                    .map(userMapper::selectById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<Integer, String> userMap = users.stream()
                    .collect(Collectors.toMap(
                            User::getId,
                            User::getAccount,
                            (k1, k2) -> k1
                    ));

            // 转换为VO对象并添加排名
            List<GameScoreVO> result = new ArrayList<>();
            for (int i = 0; i < scores.size(); i++) {
                GameScore score = scores.get(i);
                GameScoreVO vo = new GameScoreVO();
                vo.setRank(i + 1);
                vo.setUsername(userMap.getOrDefault(score.getUserId(), "未知用户"));
                vo.setScore(score.getScore());
                vo.setCreateTime(score.getCreateTime());
                result.add(vo);
            }

            return result;

        } catch (Exception e) {
            log.error("获取排行榜失败", e);
            throw new RuntimeException("获取排行榜失败");
        }
    }

    @Override
    public Integer getUserBestScore(Integer userId, String gameType) {
        try {
            LambdaQueryWrapper<GameScore> wrapper = Wrappers.<GameScore>lambdaQuery()
                    .eq(GameScore::getUserId, userId)
                    .eq(GameScore::getGameType, gameType)
                    .orderByDesc(GameScore::getScore)
                    .last("LIMIT 1");

            GameScore score = gameScoreMapper.selectOne(wrapper);
            return score != null ? score.getScore() : 0;
        } catch (Exception e) {
            log.error("获取用户最高分失败 - userId: {}, gameType: {}", userId, gameType, e);
            return 0;
        }
    }

    @Override
    public Map<String, Integer> getUserAllBestScores(Integer userId) {
        try {
            // 获取所有游戏类型的最高分
            Map<String, Integer> result = new HashMap<>();
            Arrays.asList("2048", "snake", "memory").forEach(gameType -> {
                Integer bestScore = getUserBestScore(userId, gameType);
                result.put(gameType, bestScore);
            });
            return result;
        } catch (Exception e) {
            log.error("获取用户所有游戏最高分失败 - userId: {}", userId, e);
            return Collections.emptyMap();
        }
    }
}