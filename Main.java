package com.example.maze;

import com.example.maze.MainWindows.LoginFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 设置系统属性，使用 JRE 内置的时区数据
        System.setProperty("java.locale.providers", "JRE,CLDR");
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
