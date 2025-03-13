package com.example.maze.Sound;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SoundPlayer {
    // 播放单次音效
    public static void playSound(String filePath) {
        new Thread(() -> {
            try (InputStream is = SoundPlayer.class.getResourceAsStream(filePath)) {
                if (is == null) {
                    System.err.println("找不到音效资源：" + filePath);
                    return;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                byte[] audioBytes = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bais);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    // 播放背景音乐，循环播放
    public static Clip playBackgroundMusic(String filePath) {
        Clip clip = null;
        try (InputStream is = SoundPlayer.class.getResourceAsStream(filePath)) {
            if (is == null) {
                System.err.println("找不到背景音乐资源：" + filePath);
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            byte[] audioBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bais);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
        return clip;
    }

    // 停止背景音乐（增加 flush() 操作以确保彻底停止）
    public static void stopMusic(Clip clip) {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.flush();
            clip.close();
        }
    }
}
