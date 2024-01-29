package org.example;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class MusicPlayer {

    private Clip clip;
    private boolean isPaused = false;

    private static MusicPlayer instance;

    private MusicPlayer(String musicFilePath) {
        initializeMusic(musicFilePath);

        // 创建新线程并启动
        Thread musicThread = new Thread(this::playMusicLoop);
        musicThread.start();
    }

    public static MusicPlayer getInstance(String musicFilePath) {
        if (instance == null) {
            instance = new MusicPlayer(musicFilePath);
        }
        return instance;
    }

    private void initializeMusic(String path) {
        try {
            // 获取资源的 URL
            URL audioFileURL = getClass().getClassLoader().getResource(path);
            System.out.println("Audio File URL: " + audioFileURL);

            if (audioFileURL != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFileURL);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
                // 获取 FloatControl，用于控制音量
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                // 设置音量（负值表示降低音量，正值表示增加音量）
                gainControl.setValue(gainControl.getValue() - 10f);

            } else {
                System.err.println("音乐文件未找到");
            }

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playMusicLoop() {
        if (clip != null) {
            // 循环播放
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            try {
                // 让线程休眠，保持程序运行
                while (true) {
                    Thread.sleep(1000);
                    // 检查是否暂停
                    if (isPaused) {
                        clip.stop();
                    } else {
                        // 如果没有暂停，继续播放
                        clip.start();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 暂停音乐
    public void pause() {
        isPaused = true;
    }

    // 恢复音乐
    public void resume() {
        isPaused = false;
    }
}
