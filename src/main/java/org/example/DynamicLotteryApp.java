package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DynamicLotteryApp {
    private JFrame frame;
    private JPanel buttonPanel;
    private JPanel labelPanel;
    private List<String> totalParticipantList;
    private List<String> currentLotteryList;
    private boolean isDrawing;
    private ScheduledExecutorService executorService;
    private JButton drawButton;
    private JButton stopButton;
    private JButton clearButton;
    private JButton exitButton;
    private JRadioButton specialPrizeRadio;
    private JRadioButton firstPrizeRadio;
    private JRadioButton secondPrizeRadio;

    private HashMap<String, String> employeeMap;
    private LuckyDraw drawer;

    public DynamicLotteryApp() {
        initialize();
    }

    private JLabel createBackgroundLabel(ImageIcon bg) {
        return new JLabel(bg) {
            @Override
            public void paint(Graphics g) {
                Image img = bg.getImage();
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
            }
        };
    }

    private void initMainForm(JFrame frame, JLabel label) {
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                label.setSize(frame.getSize());
                label.repaint();
            }
        });
    }

    private void initMainFrameSize(JFrame frame, JLabel label) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(d.width, d.height);
        label.setSize(frame.getSize());
    }


    private void initialize() {
        frame = new JFrame("实时抽奖程序");
        ImageIcon bg = new ImageIcon(DynamicLotteryApp.class.getResource("/images/bg.png"));
        JLabel label = createBackgroundLabel(bg);
        initMainForm(frame, label);
        initMainFrameSize(frame, label);

        initGlobalFont();
        // 获取默认屏幕设备
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // 设置全屏
        frame.setUndecorated(true);
        frame.setSize(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());

        totalParticipantList = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            totalParticipantList.add("参与者" + i);
        }

        currentLotteryList = new ArrayList<>();

        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout());
        frame.add(buttonPanel, BorderLayout.NORTH);

        drawButton = new JButton("开始抽奖");
        drawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDrawing();
            }
        });
        buttonPanel.add(drawButton);

        stopButton = new JButton("停止抽奖");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopDrawing();
            }
        });
        buttonPanel.add(stopButton);

        clearButton = new JButton("清空中奖者");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearWinners();
            }
        });
        buttonPanel.add(clearButton);

        specialPrizeRadio = new JRadioButton("特等奖");
        firstPrizeRadio = new JRadioButton("一等奖");
        secondPrizeRadio = new JRadioButton("二等奖");

        ButtonGroup prizeGroup = new ButtonGroup();
        prizeGroup.add(specialPrizeRadio);
        prizeGroup.add(firstPrizeRadio);
        prizeGroup.add(secondPrizeRadio);

        buttonPanel.add(specialPrizeRadio);
        buttonPanel.add(firstPrizeRadio);
        buttonPanel.add(secondPrizeRadio);

        exitButton = new JButton("退出抽奖");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        buttonPanel.add(exitButton);

        labelPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        frame.getLayeredPane().add(labelPanel, Integer.MIN_VALUE);
        labelPanel.setLayout(new GridBagLayout()); // 使用 GridBagLayout
        frame.add(labelPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        ExcelReader reader = new ExcelReader();
        employeeMap = reader.read();
        drawer = new LuckyDraw();
    }

    private void initGlobalFont() {
        Font globalFont = new Font("微软雅黑", Font.PLAIN, 14);
        UIManager.put("Button.font", globalFont);
        UIManager.put("Label.font", globalFont);
        UIManager.put("RadioButton.font", globalFont);
        UIManager.put("TextField.font", globalFont);
    }

    private void startDrawing() {
        if (!isDrawing) {
            if (!specialPrizeRadio.isSelected() && !firstPrizeRadio.isSelected() && !secondPrizeRadio.isSelected()) {
                JOptionPane.showMessageDialog(frame, "请选中要抽的奖项！", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }

            specialPrizeRadio.setEnabled(false);
            firstPrizeRadio.setEnabled(false);
            secondPrizeRadio.setEnabled(false);

            isDrawing = true;
            drawButton.setEnabled(false);
            clearButton.setEnabled(false);
            stopButton.setEnabled(true);

            int selectedPrize = getSelectedPrize();

            executorService = Executors.newSingleThreadScheduledExecutor();

            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    updateLotteryList(selectedPrize);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
        }
    }

    private void stopDrawing() {
        if (isDrawing) {
            specialPrizeRadio.setEnabled(true);
            firstPrizeRadio.setEnabled(true);
            secondPrizeRadio.setEnabled(true);

            isDrawing = false;
            drawButton.setEnabled(true);
            stopButton.setEnabled(false);
            clearButton.setEnabled(true);

            executorService.shutdown();
        }
    }

    private void updateLotteryList(int selectedPrize) {
        List<String> shuffledList = new ArrayList<>(totalParticipantList);
        Collections.shuffle(shuffledList);
        List<String> winnersList;
        currentLotteryList.clear();

        switch (selectedPrize) {
            case 1:
                winnersList = drawer.drawPrize(employeeMap, 1);
                currentLotteryList.add(String.format("%s %s", winnersList.get(0), employeeMap.get(winnersList.get(0))));
                break;
            case 2:
                winnersList = drawer.drawPrize(employeeMap, 3);
                for (int i = 0; i < 3; i++) {
                    currentLotteryList.add(String.format("%s %s", winnersList.get(i), employeeMap.get(winnersList.get(i))));
                }
                break;
            case 3:
                winnersList = drawer.drawPrize(employeeMap, 10);
                for (int i = 0; i < 10; i++) {
                    currentLotteryList.add(String.format("%s %s", winnersList.get(i), employeeMap.get(winnersList.get(i))));
                }
                break;
            default:
                break;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showCurrentLotteryList();
            }
        });
    }

    private void showCurrentLotteryList() {
        labelPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // 设置间隔

        for (String participant : currentLotteryList) {
            JLabel label = new JLabel(participant);
            label.setFont(new Font("微软雅黑", Font.PLAIN, 50));  // 设置字体大小为18
            labelPanel.add(label, gbc);
            gbc.gridy++;
        }

        labelPanel.revalidate();
        labelPanel.repaint();
    }

    private void clearWinners() {
        currentLotteryList.clear();
        labelPanel.removeAll();
        labelPanel.revalidate();
        labelPanel.repaint();
    }

    private int getSelectedPrize() {
        if (specialPrizeRadio.isSelected()) {
            return 1;
        } else if (firstPrizeRadio.isSelected()) {
            return 2;
        } else if (secondPrizeRadio.isSelected()) {
            return 3;
        } else {
            return 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DynamicLotteryApp();
            }
        });
    }
}
