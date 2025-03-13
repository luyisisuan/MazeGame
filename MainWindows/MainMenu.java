package com.example.maze.MainWindows;

import com.example.maze.Achievement.AchievementsFrame;
import com.example.maze.Game.GameState;
import com.example.maze.Game.MazeGame;
import com.example.maze.User.User;
import com.example.maze.User.UserManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class MainMenu extends JFrame {
    private JComboBox<String> difficultyCombo;
    private JButton startButton;
    private JButton continueButton;
    private JButton achievementsButton; // 新增：成就按钮
    private JButton settingsButton;       // 自定义设置按钮
    private JButton leaderboardButton;

    // 保存自定义设置的参数
    private int customRows = 0;
    private int customCols = 0;
    private double customExtraPassageProbability = 0.0;
    private long customMoveCooldown = 200; // 默认200ms
    // 标记是否使用自定义设置
    private boolean useCustomSettings = false;

    // 用户相关
    private User currentUser;
    private JLabel userLabel; // 显示当前用户信息
    private UserManager userManager;

    public MainMenu(User user) {
        userManager = new UserManager();
        setCurrentUser(user);
        setTitle("迷宫游戏 - 主菜单");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        // 顶部区域：显示当前用户信息
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        userLabel = new JLabel("当前用户: " + currentUser.getUsername(), SwingConstants.CENTER);
        userLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        userLabel.setForeground(new Color(0, 102, 204));
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        userInfoPanel.setOpaque(false);
        userInfoPanel.add(userLabel);
        topContainer.add(userInfoPanel);
        add(topContainer, BorderLayout.NORTH);

        // 中间区域：使用 BoxLayout 垂直排列组件
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 图标展示
        ImageIcon icon = new ImageIcon("icon.png"); // 请确保路径正确
        Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        icon = new ImageIcon(img);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(iconLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 难度选择区域
        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        difficultyPanel.setOpaque(false);
        JLabel difficultyLabel = new JLabel("选择难度:");
        difficultyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        String[] difficulties = {"简单", "普通", "困难"};
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        difficultyPanel.add(difficultyLabel);
        difficultyPanel.add(difficultyCombo);
        centerPanel.add(difficultyPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 按钮顺序：开始游戏 → 继续游戏 → 自定义设置 → 成就 → 排行榜
        startButton = new JButton("开始游戏");
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(startButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        continueButton = new JButton("继续游戏");
        continueButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (!new File("gamestate.dat").exists()) {
            continueButton.setEnabled(false);
        }
        centerPanel.add(continueButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        settingsButton = new JButton("自定义设置");
        settingsButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(settingsButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        achievementsButton = new JButton("成就");
        achievementsButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        achievementsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(achievementsButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        leaderboardButton = new JButton("排行榜");
        leaderboardButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        leaderboardButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(leaderboardButton);

        add(centerPanel, BorderLayout.CENTER);

        setSize(600, 700);
        setLocationRelativeTo(null);

        // 按钮事件
        startButton.addActionListener(e -> {
            if (currentUser == null) {
                JOptionPane.showMessageDialog(MainMenu.this, "请先登录或注册！");
                return;
            }
            if (useCustomSettings) {
                MazeGame game = new MazeGame(customRows, customCols, customExtraPassageProbability, customMoveCooldown);
                game.setCurrentUser(currentUser);
                game.setVisible(true);
            } else {
                String difficulty = (String) difficultyCombo.getSelectedItem();
                int size;
                if ("简单".equals(difficulty)) {
                    size = 15;
                } else if ("普通".equals(difficulty)) {
                    size = 25;
                } else {
                    size = 35;
                }
                MazeGame game = new MazeGame(size, size);
                game.setCurrentUser(currentUser);
                game.setVisible(true);
            }
            dispose();
        });

        continueButton.addActionListener(e -> {
            if (currentUser == null) {
                JOptionPane.showMessageDialog(MainMenu.this, "请先登录或注册！");
                return;
            }
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("gamestate.dat"))) {
                GameState state = (GameState) ois.readObject();
                MazeGame game = new MazeGame(state);
                game.setCurrentUser(currentUser);
                game.setVisible(true);
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(MainMenu.this, "加载游戏失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        settingsButton.addActionListener(e -> openSettingsDialog());

        // 在 MainMenu 中增加成就按钮的事件：
        achievementsButton.addActionListener(e -> {
            new AchievementsFrame();
        });

        leaderboardButton.addActionListener(e -> {
            String leaderboardText = Leaderboard.getLeaderboardText();
            JOptionPane.showMessageDialog(MainMenu.this, leaderboardText, "排行榜", JOptionPane.INFORMATION_MESSAGE);
        });

        setVisible(true);
    }

    // 打开自定义设置对话框
    private void openSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            customRows = dialog.getRows();
            customCols = dialog.getCols();
            customExtraPassageProbability = dialog.getExtraPassageProbability();
            customMoveCooldown = dialog.getMoveCooldown();
            useCustomSettings = true;
        }
    }

    // 设置当前登录用户
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (userLabel != null) {
            userLabel.setText("当前用户: " + user.getUsername());
        }
    }
}
