package com.example.maze.Achievement;

import java.io.Serializable;
import java.util.Date;

public class Achievement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;          // 成就名称
    private String description;   // 成就描述
    private boolean unlocked;     // 是否解锁
    private Date achievedTime;    // 解锁时间

    public Achievement(String name, String description) {
        this.name = name;
        this.description = description;
        this.unlocked = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public Date getAchievedTime() {
        return achievedTime;
    }

    public void unlock() {
        if (!unlocked) {
            unlocked = true;
            achievedTime = new Date();
            System.out.println("成就解锁: " + name);
        }
    }
}
