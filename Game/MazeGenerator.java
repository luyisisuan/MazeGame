package com.example.maze.Game;

import com.example.maze.Game.Maze;

import java.util.*;

public class MazeGenerator {
    private Maze maze;
    private Random rand;
    private Maze.Cell entrance;
    private Maze.Cell exit;

    public MazeGenerator(Maze maze) {
        this.maze = maze;
        this.rand = new Random();
    }

    /**
     * 生成迷宫：
     * 1. 入口设为迷宫中心（小人从此开始走）
     * 2. 使用 DFS 生成完美迷宫
     * 3. 随机去除部分墙壁，增加循环，提升难度
     * 4. 通过 BFS 计算距离，从较远候选单元中随机选择一个作为出口
     * 5. 随机为部分单元分配陷阱（避开入口和出口）
     */
    public void generate() {
        int rows = maze.getRows();
        int cols = maze.getCols();
        Maze.Cell[][] grid = maze.getGrid();

        // 初始化所有单元格
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].visited = false;
                grid[r][c].top = true;
                grid[r][c].bottom = true;
                grid[r][c].left = true;
                grid[r][c].right = true;
                grid[r][c].trapType = 0;
                grid[r][c].trapTriggered = false;
            }
        }

        // 设置入口为迷宫中心
        int startRow = rows / 2;
        int startCol = cols / 2;
        entrance = grid[startRow][startCol];
        entrance.visited = true;

        // 使用 DFS 生成完美迷宫
        generateMazeDFS(entrance, grid, rows, cols);

        // 随机去除部分墙壁，增加循环，提升难度
        addLoops(grid, rows, cols);

        // 通过 BFS 计算各单元到入口的距离，并随机选择一个较远的单元作为出口
        exit = generateRandomExit(entrance, grid);

        // 设置入口和出口到 Maze 对象中
        maze.setEntrance(entrance);
        maze.setExit(exit);

        // 随机为部分单元分配陷阱（避开入口和出口）
        addTraps(grid, rows, cols);
    }

    private void generateMazeDFS(Maze.Cell start, Maze.Cell[][] grid, int rows, int cols) {
        Stack<Maze.Cell> stack = new Stack<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            Maze.Cell current = stack.peek();
            List<Maze.Cell> neighbors = getUnvisitedNeighbors(current, grid, rows, cols);
            if (!neighbors.isEmpty()) {
                Maze.Cell next = neighbors.get(rand.nextInt(neighbors.size()));
                removeWallBetween(current, next);
                next.visited = true;
                stack.push(next);
            } else {
                stack.pop();
            }
        }
    }

    private List<Maze.Cell> getUnvisitedNeighbors(Maze.Cell cell, Maze.Cell[][] grid, int rows, int cols) {
        List<Maze.Cell> neighbors = new ArrayList<>();
        int r = cell.row, c = cell.col;
        if (r > 0 && !grid[r - 1][c].visited) neighbors.add(grid[r - 1][c]);      // 上
        if (c < cols - 1 && !grid[r][c + 1].visited) neighbors.add(grid[r][c + 1]);  // 右
        if (r < rows - 1 && !grid[r + 1][c].visited) neighbors.add(grid[r + 1][c]);  // 下
        if (c > 0 && !grid[r][c - 1].visited) neighbors.add(grid[r][c - 1]);         // 左
        Collections.shuffle(neighbors, rand);
        return neighbors;
    }

    private void removeWallBetween(Maze.Cell current, Maze.Cell next) {
        if (next.row == current.row - 1) {
            current.top = false;
            next.bottom = false;
        } else if (next.row == current.row + 1) {
            current.bottom = false;
            next.top = false;
        } else if (next.col == current.col - 1) {
            current.left = false;
            next.right = false;
        } else if (next.col == current.col + 1) {
            current.right = false;
            next.left = false;
        }
    }

    private void addLoops(Maze.Cell[][] grid, int rows, int cols) {
        double loopProbability = 0.05; // 5% 概率随机移除一堵墙
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Maze.Cell cell = grid[r][c];
                if (r > 0 && cell.top && rand.nextDouble() < loopProbability) {
                    cell.top = false;
                    grid[r - 1][c].bottom = false;
                }
                if (c < cols - 1 && cell.right && rand.nextDouble() < loopProbability) {
                    cell.right = false;
                    grid[r][c + 1].left = false;
                }
                if (r < rows - 1 && cell.bottom && rand.nextDouble() < loopProbability) {
                    cell.bottom = false;
                    grid[r + 1][c].top = false;
                }
                if (c > 0 && cell.left && rand.nextDouble() < loopProbability) {
                    cell.left = false;
                    grid[r][c - 1].right = false;
                }
            }
        }
    }

    private Maze.Cell generateRandomExit(Maze.Cell start, Maze.Cell[][] grid) {
        int rows = maze.getRows();
        int cols = maze.getCols();
        int[][] dist = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            Arrays.fill(dist[r], -1);
        }
        Queue<Maze.Cell> queue = new LinkedList<>();
        queue.add(start);
        dist[start.row][start.col] = 0;
        int d_max = 0;
        while (!queue.isEmpty()) {
            Maze.Cell cur = queue.poll();
            int r = cur.row, c = cur.col;
            d_max = Math.max(d_max, dist[r][c]);
            if (!cur.top && r > 0 && dist[r - 1][c] == -1) {
                dist[r - 1][c] = dist[r][c] + 1;
                queue.add(grid[r - 1][c]);
            }
            if (!cur.right && c < cols - 1 && dist[r][c + 1] == -1) {
                dist[r][c + 1] = dist[r][c] + 1;
                queue.add(grid[r][c + 1]);
            }
            if (!cur.bottom && r < rows - 1 && dist[r + 1][c] == -1) {
                dist[r + 1][c] = dist[r][c] + 1;
                queue.add(grid[r + 1][c]);
            }
            if (!cur.left && c > 0 && dist[r][c - 1] == -1) {
                dist[r][c - 1] = dist[r][c] + 1;
                queue.add(grid[r][c - 1]);
            }
        }
        List<Maze.Cell> candidateExits = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (dist[r][c] >= d_max / 2) {
                    candidateExits.add(grid[r][c]);
                }
            }

        }
        if (candidateExits.isEmpty()) {
            return start;
        }
        return candidateExits.get(rand.nextInt(candidateExits.size()));
    }

    // 随机为部分单元分配陷阱
    private void addTraps(Maze.Cell[][] grid, int rows, int cols) {
        double trapProbability = 0.02; // 2% 概率
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // 避免在入口和出口放陷阱
                if (grid[r][c] == entrance || grid[r][c] == exit) continue;
                if (rand.nextDouble() < trapProbability) {
                    // 随机选择陷阱类型 1~3
                    grid[r][c].trapType = rand.nextInt(3) + 1;
                }
            }
        }
    }

    public Maze.Cell getEntrance() {
        return entrance;
    }

    public Maze.Cell getExit() {
        return exit;
    }
}
