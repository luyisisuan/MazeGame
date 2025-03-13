package com.example.maze.Game;

import java.io.Serializable;

public class Maze implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rows;
    private int cols;
    private Cell[][] grid;

    // 入口和出口（由 MazeGenerator 设置）
    private Cell entrance;
    private Cell exit;

    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(r, c);
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public Cell getCell(int r, int c) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            return grid[r][c];
        }
        return null;
    }

    public Cell getEntrance() {
        return entrance;
    }

    public void setEntrance(Cell entrance) {
        this.entrance = entrance;
    }

    public Cell getExit() {
        return exit;
    }

    public void setExit(Cell exit) {
        this.exit = exit;
    }

    public static class Cell implements Serializable {
        private static final long serialVersionUID = 1L;

        public int row, col;
        public boolean top = true, bottom = true, left = true, right = true;
        public boolean visited = false;

        // 陷阱属性：trapType：0=无陷阱；1=地面塌陷；2=毒气；3=地雷
        public int trapType = 0;
        // 标记陷阱是否已触发
        public boolean trapTriggered = false;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
