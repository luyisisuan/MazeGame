package com.example.maze.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementManager {
    private static AchievementManager instance = null;
    private List<Achievement> achievements;
    private int consecutiveWins = 0;

    AchievementManager() {
        achievements = new ArrayList<>();
        achievements.add(new Achievement("最短步数通关", "以50步或更少通关迷宫"));
        achievements.add(new Achievement("最快通关", "在30秒内通关迷宫"));
        achievements.add(new Achievement("连续通关", "连续通关3次"));
    }

    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void addWin() {
        consecutiveWins++;
    }

    public void resetConsecutiveWins() {
        consecutiveWins = 0;
    }

    // 根据当前游戏成绩更新成就
    public void updateAchievements(int steps, int time) {
        if (steps <= 50) {
            Achievement a = getAchievementByName("最短步数通关");
            if (a != null) a.unlock();
        }
        if (time <= 30) {
            Achievement a = getAchievementByName("最快通关");
            if (a != null) a.unlock();
        }
        if (consecutiveWins >= 3) {
            Achievement a = getAchievementByName("连续通关");
            if (a != null) a.unlock();
        }
    }

    private Achievement getAchievementByName(String name) {
        for (Achievement a : achievements) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }
}
