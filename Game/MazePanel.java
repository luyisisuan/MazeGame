package com.example.maze.Game;

import com.example.maze.Game.Maze;
import com.example.maze.Game.MazeGame;
import com.example.maze.Game.MazeGameHolder;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;

public class MazePanel extends JPanel {
    private Maze maze;
    private int rows, cols;
    private Image playerImage;
    private Image treasureImage;
    private Image enemyImage;
    private final int defaultCellSize = 20;

    // 可视半径（迷雾效果）
    private int visibleRadius = 3;

    // 敌人对象，由 MazeGame 设置
    private MazeGame.Enemy enemy;

    public MazePanel(Maze maze) {
        this(maze, 3);
    }

    public MazePanel(Maze maze, int visibleRadius) {
        this.maze = maze;
        this.visibleRadius = visibleRadius;
        this.rows = maze.getRows();
        this.cols = maze.getCols();
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(cols * defaultCellSize, rows * defaultCellSize));
        setMinimumSize(new Dimension(cols * defaultCellSize, rows * defaultCellSize));

        // 加载玩家图片
        try {
            playerImage = ImageIO.read(this.getClass().getResource("/images/monkey.png"));
        } catch (IOException ex) {
            playerImage = null;
            System.err.println("无法加载图片：/images/jkh.png");
        }
        // 加载出口（宝箱）图片
        try {
            treasureImage = ImageIO.read(this.getClass().getResource("/images/treasure.png"));
        } catch (IOException ex) {
            treasureImage = null;
            System.err.println("无法加载图片：/images/treasure.png");
        }
        // 加载敌人图片
        try {
            enemyImage = ImageIO.read(this.getClass().getResource("/images/bot.png"));
        } catch (IOException ex) {
            enemyImage = null;
            System.err.println("无法加载图片：/images/jkh.png");
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(cols * defaultCellSize, rows * defaultCellSize);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int cellSize = Math.min(panelWidth / cols, panelHeight / rows);
        if (cellSize <= 0) {
            cellSize = defaultCellSize;
        }

        Maze.Cell[][] grid = maze.getGrid();

        // 绘制迷宫墙体
        g.setColor(Color.BLACK);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * cellSize;
                int y = r * cellSize;
                Maze.Cell cell = grid[r][c];
                if (cell.top) {
                    g.drawLine(x, y, x + cellSize, y);
                }
                if (cell.left) {
                    g.drawLine(x, y, x, y + cellSize);
                }
                if (cell.bottom) {
                    g.drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                }
                if (cell.right) {
                    g.drawLine(x + cellSize, y, x + cellSize, y + cellSize);
                }
            }
        }

        // 绘制出口（宝箱）
        int exitRow, exitCol;
        if (maze.getExit() != null) {
            exitRow = maze.getExit().row;
            exitCol = maze.getExit().col;
        } else {
            exitRow = rows - 1;
            exitCol = cols - 1;
        }
        int exitX = exitCol * cellSize;
        int exitY = exitRow * cellSize;
        if (treasureImage != null) {
            g.drawImage(treasureImage, exitX, exitY, cellSize, cellSize, this);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(exitX + 2, exitY + 2, cellSize - 4, cellSize - 4);
        }

        // 绘制玩家
        int playerColIndex = MazeGameHolder.playerCol;
        int playerRowIndex = MazeGameHolder.playerRow;
        int playerX = playerColIndex * cellSize;
        int playerY = playerRowIndex * cellSize;
        if (playerImage != null) {
            g.drawImage(playerImage, playerX, playerY, cellSize, cellSize, this);
        } else {
            g.setColor(Color.RED);
            int size = cellSize / 2;
            g.fillOval(playerX + cellSize / 4, playerY + cellSize / 4, size, size);
        }

        // 绘制敌人
        if (enemy != null) {
            int enemyX = enemy.col * cellSize;
            int enemyY = enemy.row * cellSize;
            if (enemyImage != null) {
                g.drawImage(enemyImage, enemyX, enemyY, cellSize, cellSize, this);
            } else {
                g.setColor(Color.MAGENTA);
                g.fillRect(enemyX + 2, enemyY + 2, cellSize - 4, cellSize - 4);
            }
        }

        // 绘制迷雾效果：玩家视野以外区域覆盖半透明黑色
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 240));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int distance = Math.abs(r - playerRowIndex) + Math.abs(c - playerColIndex);
                if (distance > visibleRadius) {
                    int x = c * cellSize;
                    int y = r * cellSize;
                    g2.fillRect(x, y, cellSize, cellSize);
                }
            }
        }
        g2.dispose();
    }

    public void setPlayerPosition(int row, int col) {
        MazeGameHolder.playerRow = row;
        MazeGameHolder.playerCol = col;
        repaint();
    }

    // 设置敌人对象
    public void setEnemy(MazeGame.Enemy enemy) {
        this.enemy = enemy;
        repaint();
    }
}
