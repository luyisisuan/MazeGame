package com.example.maze.MainWindows;

import com.example.maze.User.User;
import com.example.maze.User.UserManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private UserManager userManager;

    public LoginFrame() {
        userManager = new UserManager();
        setTitle("迷宫游戏 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);

        // 主面板采用垂直 BoxLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel headerLabel = new JLabel("欢迎登录", SwingConstants.CENTER);
        headerLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 表单面板：用户名和密码输入框
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setOpaque(false);
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        usernameField = new JTextField();
        JLabel passLabel = new JLabel("密码:");
        passLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        passwordField = new JPasswordField();
        formPanel.add(userLabel);
        formPanel.add(usernameField);
        formPanel.add(passLabel);
        formPanel.add(passwordField);
        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        mainPanel.add(buttonPanel);

        add(mainPanel);

        // 登录按钮事件
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            User user = userManager.loginUser(username, password);
            if (user != null) {
                JOptionPane.showMessageDialog(LoginFrame.this, "登录成功！");
                new MainMenu(user);
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, "登录失败，请检查用户名或密码！");
            }
        });

        // 注册按钮事件：跳转到注册界面
        registerButton.addActionListener(e -> {
            new RegisterFrame();
            dispose();
        });

        setVisible(true);
    }
}
//登录界面