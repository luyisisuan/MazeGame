package com.example.maze.Achievement;

import com.example.maze.Achievement.Achievement;
import com.example.maze.Achievement.AchievementManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

public class AchievementsFrame extends JFrame {
    public AchievementsFrame() {
        setTitle("个人成就");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Achievement a : AchievementManager.getInstance().getAchievements()) {
            String text = a.getName() + " - " + a.getDescription();
            if (a.isUnlocked()) {
                text += " (解锁于：" + sdf.format(a.getAchievedTime()) + ")";
            } else {
                text += " (未解锁)";
            }
            JLabel label = new JLabel(text);
            label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            mainPanel.add(label);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        add(new JScrollPane(mainPanel));
        setVisible(true);
    }
}
