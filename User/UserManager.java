package com.example.maze.User;

import com.example.maze.User.User;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static final String USER_FILE = "users.dat";
    private Map<String, User> userDatabase;

    public UserManager() {
        loadUsers();
    }

    // 从文件加载用户数据，如果文件不存在则初始化一个空的 Map
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            userDatabase = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            userDatabase = (Map<String, User>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            userDatabase = new HashMap<>();
        }
    }

    // 将用户数据保存到文件
    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(userDatabase);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 注册新用户，若用户名已存在返回 false，否则保存新用户并返回 true
    public boolean registerUser(String username, String password) {
        if (userDatabase.containsKey(username)) {
            return false;
        }
        User user = new User(username, password);
        userDatabase.put(username, user);
        saveUsers();
        return true;
    }

    // 登录用户，验证用户名和密码，成功返回对应的 User 对象，否则返回 null
    public User loginUser(String username, String password) {
        User user = userDatabase.get(username);
        if (user != null && user.checkPassword(password)) {
            return user;
        }
        return null;
    }
}
