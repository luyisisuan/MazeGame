package com.example.maze.Game;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // 迷宫状态
    public Maze maze;
    // 玩家当前行列位置
    public int playerRow;
    public int playerCol;
    // 已经走的步数
    public int steps;
    // 已用时间（秒）
    public long elapsedTime;
    // 迷宫的行数和列数（用于确定难度）
    public int rows;
    public int cols;
    // 额外通路生成概率（迷宫密度设置，范围 0~0.5）
    public double extraPassageProbability;
    // 移动冷却时间（单位毫秒，控制移动速度）
    public long moveCooldown;
    public int playerLife;
}
//自定义关卡难度