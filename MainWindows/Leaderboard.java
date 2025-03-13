package com.example.maze.MainWindows;

import java.util.prefs.Preferences;

public class Leaderboard {
    private static final Preferences prefs = Preferences.userNodeForPackage(Leaderboard.class);

    public static int getBestTime(String difficulty) {
        // 默认值 9999 秒表示没有记录
        return prefs.getInt("bestTime_" + difficulty, 9999);
    }

    public static int getBestSteps(String difficulty) {
        // 默认值 9999 步表示没有记录
        return prefs.getInt("bestSteps_" + difficulty, 9999);
    }

    /**
     * 更新记录：如果本次时间或步数更好，则保存新纪录。
     */
    public static void updateRecord(String difficulty, int time, int steps) {
        int bestTime = getBestTime(difficulty);
        int bestSteps = getBestSteps(difficulty);
        if (time < bestTime) {
            prefs.putInt("bestTime_" + difficulty, time);
        }
        if (steps < bestSteps) {
            prefs.putInt("bestSteps_" + difficulty, steps);
        }
    }

    /**
     * 返回排行榜信息文本
     */
    public static String getLeaderboardText() {
        String[] difficulties = {"简单", "普通", "困难"};
        StringBuilder sb = new StringBuilder();
        for (String diff : difficulties) {
            int time = getBestTime(diff);
            int steps = getBestSteps(diff);
            sb.append("难度: ").append(diff).append("\n")
                    .append("最快时间: ").append(time == 9999 ? "暂无" : time + "秒").append("\n")
                    .append("最少步数: ").append(steps == 9999 ? "暂无" : steps).append("\n\n");
        }
        return sb.toString();
    }
}//排行榜
