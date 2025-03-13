package com.example.maze.MainWindows;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class SettingsDialog extends JDialog {
    private boolean confirmed = false;
    private JTextField rowsField;
    private JTextField colsField;
    private JTextField densityField;
    private JTextField cooldownField;

    public SettingsDialog(JFrame parent) {
        super(parent, "自定义设置", true);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("迷宫行数:"));
        rowsField = new JTextField("25");
        panel.add(rowsField);

        panel.add(new JLabel("迷宫列数:"));
        colsField = new JTextField("25");
        panel.add(colsField);

        panel.add(new JLabel("额外通路概率(0~0.5):"));
        densityField = new JTextField("0.0");
        panel.add(densityField);

        panel.add(new JLabel("移动冷却时间(ms):"));
        cooldownField = new JTextField("200");
        panel.add(cooldownField);

        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getRows() {
        try {
            return Integer.parseInt(rowsField.getText().trim());
        } catch(NumberFormatException e) {
            return 25;
        }
    }

    public int getCols() {
        try {
            return Integer.parseInt(colsField.getText().trim());
        } catch(NumberFormatException e) {
            return 25;
        }
    }

    public double getExtraPassageProbability() {
        try {
            double d = Double.parseDouble(densityField.getText().trim());
            if (d < 0) d = 0;
            if (d > 0.5) d = 0.5;
            return d;
        } catch(NumberFormatException e) {
            return 0.0;
        }
    }

    public long getMoveCooldown() {
        try {
            return Long.parseLong(cooldownField.getText().trim());
        } catch(NumberFormatException e) {
            return 200;
        }
    }
}
//自定义关卡难度