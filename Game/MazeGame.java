package com.example.maze.Game;

import com.example.maze.Achievement.AchievementManager;
import com.example.maze.MainWindows.Leaderboard;
import com.example.maze.MainWindows.MainMenu;
import com.example.maze.Sound.SoundPlayer;
import com.example.maze.User.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import javax.sound.sampled.Clip;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.Random;

public class MazeGame extends JFrame {
    private Maze maze;
    private MazePanel mazePanel;
    private int rows;
    private int cols;

    private long pauseStartTime = 0;      // 暂停开始时间
    private long totalPauseDuration = 0;  // 累计暂停时间


    // 玩家逻辑位置（后续将使用迷宫入口更新）
    private int playerRow = 0, playerCol = 0;
    // 步数记录
    private int steps = 0;
    // 玩家生命
    private int playerLife = 3;
    // 计时器相关
    private long startTime;
    private Timer timer;
    private JLabel statusLabel;
    private JButton restartButton;
    private JButton saveExitButton;

    // 背景音乐 Clip
    private Clip backgroundClip;

    // 用户对象（当前登录玩家）
    private User currentUser;

    // 自定义设置参数
    private double extraPassageProbability = 0.0;
    private long moveCooldown = 10; // 默认10ms
    private long lastKeyPressTime = 0;

    // 提示功能参数
    private long lastMoveTime = System.currentTimeMillis();
    private static final long INACTIVITY_THRESHOLD = 15000;
    private boolean hintShown = false;

    // 敌人相关
    private Enemy enemy;
    private Timer enemyTimer;

    // 动态迷宫定时器及随机数生成器
    private Timer dynamicMazeTimer;
    private Random dynamicRand = new Random();

    // 标志：游戏结束是否已触发，避免重复执行 gameOver()
    private boolean gameOverTriggered = false;

    // 构造方法：新游戏（默认设置）
    public MazeGame(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        initGame();
    }

    // 构造方法：新游戏，带自定义设置
    public MazeGame(int rows, int cols, double extraPassageProbability, long moveCooldown) {
        this.rows = rows;
        this.cols = cols;
        this.extraPassageProbability = extraPassageProbability;
        this.moveCooldown = moveCooldown;
        initGame();
    }

    // 构造方法：加载保存状态继续游戏
    public MazeGame(GameState state) {
        this.rows = state.rows;
        this.cols = state.cols;
        this.maze = state.maze;
        this.playerRow = state.playerRow;
        this.playerCol = state.playerCol;
        this.steps = state.steps;
        this.playerLife = state.playerLife; // 保存状态中记录了生命
        initGame();
        this.startTime = System.currentTimeMillis() - state.elapsedTime * 1000;
        lastMoveTime = System.currentTimeMillis();
    }

    // 设置当前用户
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // 初始化游戏界面和逻辑
    private void initGame() {
        setTitle("迷宫游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 启动动态迷宫定时器，每隔5秒更新部分墙壁
        dynamicMazeTimer = new Timer(10, e -> updateDynamicMaze());
        dynamicMazeTimer.start();

        // 未加载保存状态则生成新迷宫
        if (maze == null) {
            maze = new Maze(rows, cols);
            MazeGenerator generator = new MazeGenerator(maze);
            generator.generate();
        }


        // 更新玩家初始位置：使用迷宫生成的入口（迷宫中心）
        if (maze.getEntrance() != null) {
            playerRow = maze.getEntrance().row;
            playerCol = maze.getEntrance().col;
        }
        MazeGameHolder.playerRow = playerRow;
        MazeGameHolder.playerCol = playerCol;

        // 创建迷宫面板，传入迷雾可视半径（例如3）
        int fogVisibleRadius = 3;
        mazePanel = new MazePanel(maze, fogVisibleRadius);
        mazePanel.setPlayerPosition(playerRow, playerCol);
        mazePanel.setFocusable(true);
        mazePanel.requestFocusInWindow();
        add(mazePanel, BorderLayout.CENTER);

        // 状态面板：显示步数、时间及生命值
        JPanel statusPanel = new JPanel(new BorderLayout(10, 5));
        statusLabel = new JLabel("步数: 0    时间: 0s    生命: " + playerLife, SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        statusPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        restartButton = new JButton("重新开始");
        restartButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        restartButton.addActionListener(e -> restartGame());
        saveExitButton = new JButton("保存退出");
        saveExitButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        saveExitButton.addActionListener(e -> saveAndExit());
        buttonPanel.add(restartButton);
        buttonPanel.add(saveExitButton);
        statusPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(statusPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        timer = new Timer(1000, e -> updateStatus());
        timer.start();

        // 播放背景音乐（确保文件路径正确）
        backgroundClip = SoundPlayer.playBackgroundMusic("/music/FFF.wav");

        // 初始化敌人（例如，初始位置设为 (4,4)）
        enemy = new Enemy(4, 4);
        mazePanel.setEnemy(enemy);
        // 只创建一次 enemyTimer
        enemyTimer = new Timer(1000, e -> updateEnemyPosition());
        enemyTimer.start();

        // 添加键盘监听器
        mazePanel.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {
                movePlayer(e.getKeyCode());
            }
        });
    }


    private void resumeTimers() {
        timer.start();
        if (enemyTimer != null) enemyTimer.start();
        if (dynamicMazeTimer != null) dynamicMazeTimer.start();
    }



    // 恢复游戏计时器
    private void resumeGame() {
        resumeTimers(); // 直接恢复计时器，不记录暂停时间
    }


    // 更新状态标签，显示步数、时间和生命值
    private void updateStatus() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        statusLabel.setText("步数: " + steps + "    时间: " + elapsed + "s    生命: " + playerLife);
        if (!hintShown && System.currentTimeMillis() - lastMoveTime >= INACTIVITY_THRESHOLD) {
            showHint();
            hintShown = true;
        }
    }

    private void showHint() {
        // 可添加提示逻辑
    }



    // 处理玩家移动
    private void movePlayer(int keyCode) {
        long now = System.currentTimeMillis();
        if (now - lastKeyPressTime < moveCooldown) return;
        lastKeyPressTime = now;
        lastMoveTime = now;
        hintShown = false;

        // 测试按键 T：跳到出口（测试用）
        if (keyCode == KeyEvent.VK_T) {
            if (maze.getExit() != null) {
                playerRow = maze.getExit().row;
                playerCol = maze.getExit().col;
            } else {
                playerRow = maze.getRows() - 1;
                playerCol = maze.getCols() - 1;
            }
            steps++;
            SoundPlayer.playSound("/music/move.wav");
            mazePanel.setPlayerPosition(playerRow, playerCol);
            checkTrap();
            checkVictory();
            return;
        }

        Maze.Cell current = maze.getCell(playerRow, playerCol);
        int newRow = playerRow;
        int newCol = playerCol;
        if (keyCode == KeyEvent.VK_UP) {
            if (current.top) return;
            newRow--;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            if (current.bottom) return;
            newRow++;
        } else if (keyCode == KeyEvent.VK_LEFT) {
            if (current.left) return;
            newCol--;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            if (current.right) return;
            newCol++;
        }
        if (newRow >= 0 && newRow < maze.getRows() && newCol >= 0 && newCol < maze.getCols()) {
            if (newRow != playerRow || newCol != playerCol) {
                playerRow = newRow;
                playerCol = newCol;
                steps++;
                SoundPlayer.playSound("/music/move.wav");
                mazePanel.setPlayerPosition(playerRow, playerCol);
                // 检查陷阱效果
                checkTrap();
                checkVictory();
            }
        }
    }

    // 检查玩家所在单元是否触发陷阱
    private void checkTrap() {
        Maze.Cell cell = maze.getCell(playerRow, playerCol);
        if (cell.trapType > 0 && !cell.trapTriggered) {
            cell.trapTriggered = true;
            switch(cell.trapType) {
                case 1:
                    // 地面塌陷：增加5步惩罚
                    steps += 5;
                    pauseTimers();  // 暂停所有计时器
                    JOptionPane.showMessageDialog(this, "陷阱触发：地面塌陷，步数增加5步！", "陷阱", JOptionPane.WARNING_MESSAGE);
                    resumeGame();
                    break;
                case 2:
                    // 毒气区域：失去1条生命
                    playerLife--;
                    pauseTimers();  // 暂停所有计时器
                    JOptionPane.showMessageDialog(this, "陷阱触发：毒气区域，你失去1条生命！", "陷阱", JOptionPane.WARNING_MESSAGE);
                    resumeGame();
                    if (playerLife <= 0) {
                        pauseTimers();  // 暂停所有计时器
                        JOptionPane.showMessageDialog(this, "你的生命耗尽了！游戏结束！", "游戏结束", JOptionPane.ERROR_MESSAGE);
                        SoundPlayer.stopMusic(backgroundClip);
                        SoundPlayer.playSound("/music/gameover.wav");
                        gameOver();
                    }
                    break;
                case 3:
                    // 地雷：先预警，再延时1秒检查，如果玩家仍停留则游戏结束
                    pauseTimers();  // 暂停所有计时器
                    JOptionPane.showMessageDialog(this, "警告：危险区域！地雷即将爆炸，请迅速离开！", "警告", JOptionPane.WARNING_MESSAGE);
                    resumeGame();
                    Timer landmineTimer = new Timer(1000, e -> {
                        Maze.Cell currentCell = maze.getCell(playerRow, playerCol);
                        if (currentCell.trapType == 3 && currentCell.trapTriggered) {
                            SoundPlayer.stopMusic(backgroundClip);
                            pauseTimers();  // 暂停所有计时器
                            SoundPlayer.playSound("/music/gameover.wav");
                            gameOver();
                        }
                    });
                    landmineTimer.setRepeats(false);
                    landmineTimer.start();
                    break;
            }
        }
    }


    // 定义暂停所有计时器的方法
    private void pauseTimers() {
        if (timer != null) timer.stop();
        if (enemyTimer != null) enemyTimer.stop();
        if (dynamicMazeTimer != null) dynamicMazeTimer.stop();
    }




    // 使用 BFS 计算从指定起点到目标的最短路径（用于敌人追踪）
    private List<Point> findShortestPath(int startRow, int startCol, int targetRow, int targetCol) {
        int R = maze.getRows();
        int C = maze.getCols();
        boolean[][] visited = new boolean[R][C];
        Point[][] prev = new Point[R][C];
        Queue<Point> queue = new LinkedList<>();
        Point start = new Point(startRow, startCol);
        Point goal = new Point(targetRow, targetCol);
        visited[startRow][startCol] = true;
        queue.offer(start);
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            if (p.equals(goal)) break;
            Maze.Cell cell = maze.getCell(p.x, p.y);
            // 上
            if (!cell.top && p.x - 1 >= 0 && !visited[p.x - 1][p.y]) {
                Point np = new Point(p.x - 1, p.y);
                visited[np.x][np.y] = true;
                prev[np.x][np.y] = p;
                queue.offer(np);
            }
            // 下
            if (!cell.bottom && p.x + 1 < R && !visited[p.x + 1][p.y]) {
                Point np = new Point(p.x + 1, p.y);
                visited[np.x][np.y] = true;
                prev[np.x][np.y] = p;
                queue.offer(np);
            }
            // 左
            if (!cell.left && p.y - 1 >= 0 && !visited[p.x][p.y - 1]) {
                Point np = new Point(p.x, p.y - 1);
                visited[np.x][np.y] = true;
                prev[np.x][np.y] = p;
                queue.offer(np);
            }
            // 右
            if (!cell.right && p.y + 1 < C && !visited[p.x][p.y + 1]) {
                Point np = new Point(p.x, p.y + 1);
                visited[np.x][np.y] = true;
                prev[np.x][np.y] = p;
                queue.offer(np);
            }
        }
        if (!visited[goal.x][goal.y]) return null;
        LinkedList<Point> path = new LinkedList<>();
        for (Point at = goal; at != null; at = prev[at.x][at.y]) {
            path.addFirst(at);
        }
        return path;
    }

    /**
     * 每隔一定时间，随机选择若干个单元，对其某个墙壁进行切换，
     * 从而实现动态迷宫效果。
     */
    private void updateDynamicMaze() {
        int changes = 3; // 每次修改3处墙壁，可根据需要调整
        for (int i = 0; i < changes; i++) {
            // 随机选择一个非边界单元，避免 r 或 c 取到 0 或最大值
            int r = dynamicRand.nextInt(maze.getRows() - 2) + 1;
            int c = dynamicRand.nextInt(maze.getCols() - 2) + 1;
            // 如果离玩家或敌人太近，则跳过（避免影响当前路线）
            if (Math.abs(r - playerRow) + Math.abs(c - playerCol) < 2) continue;
            if (enemy != null && Math.abs(r - enemy.row) + Math.abs(c - enemy.col) < 2) continue;
            // 随机选择方向：0=上, 1=右, 2=下, 3=左
            int direction = dynamicRand.nextInt(4);
            Maze.Cell cell = maze.getCell(r, c);
            Maze.Cell neighbor = null;
            switch (direction) {
                case 0: neighbor = maze.getCell(r - 1, c); break;
                case 1: neighbor = maze.getCell(r, c + 1); break;
                case 2: neighbor = maze.getCell(r + 1, c); break;
                case 3: neighbor = maze.getCell(r, c - 1); break;
            }
            if (neighbor == null) continue;
            // 翻转当前墙壁状态
            if (direction == 0) { // 上：cell.top, neighbor.bottom
                boolean newVal = !cell.top;
                cell.top = newVal;
                neighbor.bottom = newVal;
            } else if (direction == 1) { // 右：cell.right, neighbor.left
                boolean newVal = !cell.right;
                cell.right = newVal;
                neighbor.left = newVal;
            } else if (direction == 2) { // 下：cell.bottom, neighbor.top
                boolean newVal = !cell.bottom;
                cell.bottom = newVal;
                neighbor.top = newVal;
            } else if (direction == 3) { // 左：cell.left, neighbor.right
                boolean newVal = !cell.left;
                cell.left = newVal;
                neighbor.right = newVal;
            }
        }
        mazePanel.repaint();
    }

    // 定时更新敌人位置（通过 BFS 追踪玩家，每次移动一步）
    private void updateEnemyPosition() {
        if (gameOverTriggered) return; // 如果游戏已结束，不再更新
        List<Point> path = findShortestPath(enemy.row, enemy.col, playerRow, playerCol);
        if (path != null && path.size() > 1) {
            Point next = path.get(1);
            enemy.row = next.x;
            enemy.col = next.y;
            mazePanel.repaint();

            if (enemy.row == playerRow && enemy.col == playerCol) {
                // 停止背景音乐
                SoundPlayer.stopMusic(backgroundClip);
                // 播放游戏结束音效
                SoundPlayer.playSound("/music/gameover.wav");
                JOptionPane.showMessageDialog(
                        this,
                        "你被敌人抓住了！游戏结束！",
                        "游戏结束",
                        JOptionPane.ERROR_MESSAGE
                );
                gameOver();
            }
        }
    }

    // 检查玩家是否到达出口
    private void checkVictory() {
        int exitRow, exitCol;
        if (maze.getExit() != null) {
            exitRow = maze.getExit().row;
            exitCol = maze.getExit().col;
        } else {
            exitRow = maze.getRows() - 1;
            exitCol = maze.getCols() - 1;
        }
        if (playerRow == exitRow && playerCol == exitCol) {
            timer.stop();
            enemyTimer.stop();
            dynamicMazeTimer.stop();
            SoundPlayer.stopMusic(backgroundClip);
            SoundPlayer.playSound("/music/victory.wav");
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            String difficulty = "困难";
            if (rows == 15) {
                difficulty = "简单";
            } else if (rows == 25) {
                difficulty = "普通";
            }
            Leaderboard.updateRecord(difficulty, (int) elapsed, steps);
            AchievementManager.getInstance().addWin();
            AchievementManager.getInstance().updateAchievements(steps, (int) elapsed);
            int option = JOptionPane.showConfirmDialog(this,
                    "恭喜你！你走出了迷宫！\n步数: " + steps + "\n时间: " + elapsed + "秒\n是否重新开始？",
                    "胜利", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                restartGame();
            } else {
                SoundPlayer.stopMusic(backgroundClip);
                new MainMenu(currentUser).setVisible(true);
                dispose();
            }
        }
    }

    // 游戏结束方法
    private void gameOver() {
        if (gameOverTriggered) return;
        gameOverTriggered = true;
        pauseTimers();  // 暂停所有计时器
        SoundPlayer.stopMusic(backgroundClip);
        // 如果有需要也可以播放结束音效，比如：
        // SoundPlayer.playSound("/music/gameover.wav");
        JOptionPane.showMessageDialog(this, "少侠勿灰心，请重新来过", "游戏结束", JOptionPane.ERROR_MESSAGE);
        new MainMenu(currentUser).setVisible(true);
        dispose();
    }

    // 保存游戏状态并退出
    private void saveAndExit() {
        timer.stop();
        if (enemyTimer != null) enemyTimer.stop();
        int option = JOptionPane.showConfirmDialog(this, "是否保存游戏进度以便下次继续？", "保存游戏", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            GameState state = new GameState();
            state.maze = maze;
            state.playerRow = playerRow;
            state.playerCol = playerCol;
            state.steps = steps;
            state.elapsedTime = elapsed;
            state.rows = rows;
            state.cols = cols;
            state.extraPassageProbability = extraPassageProbability;
            state.moveCooldown = moveCooldown;
            state.playerLife = playerLife;
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("gamestate.dat"))) {
                oos.writeObject(state);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        SoundPlayer.stopMusic(backgroundClip);
        new MainMenu(currentUser).setVisible(true);
        dispose();
    }

    // 重启游戏
    private void restartGame() {
        if (timer != null) timer.stop();
        if (enemyTimer != null) enemyTimer.stop();
        if (dynamicMazeTimer != null) dynamicMazeTimer.stop();
        int option = JOptionPane.showOptionDialog(this, "请选择操作", "重新开始", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, new String[]{"重新开始", "返回主界面"}, "重新开始");
        if (option == 0) {
            SoundPlayer.stopMusic(backgroundClip);
            MazeGame game = new MazeGame(rows, cols, extraPassageProbability, moveCooldown);
            game.setCurrentUser(currentUser);
            game.setVisible(true);
            dispose();
        } else {
            SoundPlayer.stopMusic(backgroundClip);
            new MainMenu(currentUser).setVisible(true);
            dispose();
        }
    }

    // 内部类：敌人
    public class Enemy {
        public int row;
        public int col;
        public Enemy(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
