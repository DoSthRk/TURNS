package com.qj.back.service;

import com.qj.back.vo.GameScoreVO;

import java.util.List;
import java.util.Map;

public interface GameScoreService {
    void saveScore(Integer userId, String gameType, Integer score);

    List<GameScoreVO> getLeaderboard(String gameType);

    Integer getUserBestScore(Integer userId, String gameType);

    Map<String, Integer> getUserAllBestScores(Integer userId);  // 添加这个方法
}
