package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DynamicLotteryApp {
    private JFrame frame;
    private JPanel buttonPanel;
    private JPanel labelPanel;
    private JPanel namePanel;
    private JLabel priceLabel;
    private JLabel imageLabel;
    private List<String> currentLotteryList;
    private List<String> winnersList;
    private final List<String> currentPriceImgPathList = new ArrayList<>();
    private final HashMap<String, String> allPriceNameMap = new HashMap<>();
    private boolean isDrawing;
    private ScheduledExecutorService executorService;
    private JButton drawButton;
    private JButton stopButton;
    private JButton clearButton;
    private JButton exitButton;
    private JButton pauseButton;
    private JRadioButton specialPrizeRadio;
    private JRadioButton firstPrizeRadio;
    private JRadioButton secondPrizeRadio;
    private JRadioButton thirdPrizeRadio;
    private JRadioButton anotherPrizeRadio;
    private int currentPrice;
    private int currentPriceIndex;
    private final HashMap<String, String> normEmployeeMap;
    private final HashMap<String, String> specEmployeeMap;
    private final LuckyDraw drawer;
    private boolean isPaused = false;

    public DynamicLotteryApp() {
        ExcelReader reader = new ExcelReader();
        normEmployeeMap = reader.read(ExcelReader.normPath);
        specEmployeeMap = reader.read(ExcelReader.specPath);
        drawer = new LuckyDraw();
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
        ImageIcon bg = new ImageIcon(DynamicLotteryApp.class.getResource("/images/bg1.jpg"));
        JLabel label = createBackgroundLabel(bg);
        initMainForm(frame, label);
        initMainFrameSize(frame, label);

        initGlobalFont();
        initPriceNameMap();
        // 获取默认屏幕设备
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // 设置全屏
        //frame.setUndecorated(true);
        frame.setSize(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());

        currentLotteryList = new ArrayList<>();

        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout());
        frame.add(buttonPanel, BorderLayout.NORTH);

        drawButton = new JButton("开始抽奖");
        AbstractAction drawButtonAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDrawing();
            }
        };
        // 开始抽奖按钮绑定回车
        drawButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressed");
        drawButton.getActionMap().put("pressed", drawButtonAction);
        drawButton.addActionListener(drawButtonAction);

        buttonPanel.add(drawButton);

        stopButton = new JButton("停止抽奖");
        AbstractAction stopButtonAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopDrawing();
            }
        };
        // 停止抽奖按钮绑定空格
        stopButton.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "pressed");
        stopButton.getActionMap().put("pressed", stopButtonAction);
        stopButton.addActionListener(stopButtonAction);

        buttonPanel.add(stopButton);

        clearButton = new JButton("兑奖");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAndSaveWinners();
            }
        });
        buttonPanel.add(clearButton);

        specialPrizeRadio = new JRadioButton("特等奖");
        firstPrizeRadio = new JRadioButton("一等奖");
        secondPrizeRadio = new JRadioButton("二等奖");
        thirdPrizeRadio = new JRadioButton("三等奖");
        anotherPrizeRadio = new JRadioButton("其他奖");

        ButtonGroup prizeGroup = new ButtonGroup();
        prizeGroup.add(thirdPrizeRadio);
        prizeGroup.add(secondPrizeRadio);
        prizeGroup.add(firstPrizeRadio);
        prizeGroup.add(specialPrizeRadio);
        prizeGroup.add(anotherPrizeRadio);

        buttonPanel.add(thirdPrizeRadio);
        buttonPanel.add(secondPrizeRadio);
        buttonPanel.add(firstPrizeRadio);
        buttonPanel.add(specialPrizeRadio);
        buttonPanel.add(anotherPrizeRadio);

        exitButton = new JButton("退出抽奖");
        AbstractAction exitButtonAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        frame,
                        "确认退出？",
                        "提示",
                        JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    // 用户点击了"是"，执行退出逻辑
                    System.exit(0);
                }
                // 如果用户点击了"否"或关闭了对话框，不执行退出逻辑
            }
        };
        exitButton.addActionListener(exitButtonAction);
        buttonPanel.add(exitButton);
        pauseButton = new JButton();
        setPlayButtonIcon("/images/pause.png");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    setPlayButtonIcon("/images/pause.png");
                    isPaused = false;
                    MusicPlayer.getInstance("background.wav").resume();
                } else {
                    setPlayButtonIcon("/images/play.png");
                    isPaused = true;
                    MusicPlayer.getInstance("background.wav").pause();
                }
            }
        });
        buttonPanel.add(pauseButton);
        labelPanel = createBackgroundPanel("/images/bg1.jpg");

        // 添加鼠标点击事件监听器
        labelPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 鼠标点击时更改背景图像
                changeBackground(labelPanel, "/images/bg2.jpg");
                labelPanel.removeMouseListener(this);
            }
        });

        namePanel = new JPanel(new GridBagLayout());
        namePanel.setOpaque(false);
        namePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 300));
        priceLabel = new JLabel();
        priceLabel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
        priceLabel.setFont(new Font("华文中宋", Font.PLAIN, 75));
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setHorizontalAlignment(JLabel.CENTER); // 居中显示
        priceLabel.setVisible(false);

        ImageIcon priceImg = new ImageIcon();
        imageLabel = new JLabel(priceImg, JLabel.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 250, 0, 0));
        labelPanel.setLayout(new BorderLayout());
        labelPanel.add(priceLabel, BorderLayout.NORTH);
        labelPanel.add(namePanel, BorderLayout.EAST); // 或 BorderLayout.WEST
        labelPanel.add(imageLabel, BorderLayout.WEST); // 或 BorderLayout.WEST

        frame.getLayeredPane().add(labelPanel, Integer.MIN_VALUE);
        frame.add(labelPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        // 退出抽奖按钮绑定ESC
        // 获取 JRootPane 的 InputMap
        InputMap inputMap = exitButton.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        // 将 AbstractAction 绑定到 ESC 键
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "pressed");
        // 获取 JRootPane 的 ActionMap
        ActionMap actionMap = exitButton.getRootPane().getActionMap();
        // 将 AbstractAction 绑定到按钮的 "pressed" 上
        actionMap.put("pressed", exitButtonAction);

        // 三等奖点击监听
        thirdPrizeRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPrice = 3;
                currentPriceIndex = 0;
                updatePriceImgPathList(currentPrice);
                updateCurrentPrice(currentPrice, currentPriceIndex);
            }
        });
        secondPrizeRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPrice = 2;
                currentPriceIndex = 0;
                updatePriceImgPathList(currentPrice);
                updateCurrentPrice(currentPrice, currentPriceIndex);
            }
        });

        firstPrizeRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPrice = 1;
                currentPriceIndex = 0;
                updatePriceImgPathList(currentPrice);
                updateCurrentPrice(currentPrice, currentPriceIndex);
            }
        });

        specialPrizeRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPrice = 0;
                currentPriceIndex = 0;
                updatePriceImgPathList(currentPrice);
                updateCurrentPrice(currentPrice, currentPriceIndex);
            }
        });
        anotherPrizeRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                priceLabel.setText("阳光普照奖：稻香村礼盒");
                priceLabel.setVisible(true);
                labelPanel.remove(imageLabel);
                labelPanel.remove(namePanel);
                ImageIcon centerPriceImg = createScaledIcon("/images/price/all.jpg", 600);
                JLabel centerImageLabel = new JLabel(centerPriceImg, JLabel.CENTER);
                labelPanel.add(centerImageLabel, BorderLayout.CENTER);
            }
        });
        MusicPlayer.getInstance("background.wav");
    }

    private void setPlayButtonIcon(String path) {
        ImageIcon icon = new ImageIcon(DynamicLotteryApp.class.getResource(path));
        // 设置要限制的大小
        int width = 20;
        int height = 20;
        // 调整图像大小
        Image resizedImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);
        pauseButton.setIcon(resizedIcon);
    }

    private void updateCurrentPrice(int selectedPrize, int index) {
        String imgPath = currentPriceImgPathList.get(index);
        String priceName = allPriceNameMap.get(imgPath);
        String prizeType = getPrizeString(selectedPrize);

        priceLabel.setText(prizeType + "：" + priceName);
        if (!priceLabel.isVisible()) {
            priceLabel.setVisible(true);
        }
        if (!imageLabel.isVisible()) {
            imageLabel.setVisible(true);
        }
        ImageIcon newIcon = createScaledIcon(imgPath, 400);
        imageLabel.setIcon(newIcon);
    }

    // 等比例缩放
    private static ImageIcon createScaledIcon(String imagePath, int baseSize) {
        ImageIcon originalIcon = new ImageIcon(DynamicLotteryApp.class.getResource(imagePath));
        Image originalImage = originalIcon.getImage();

        int originalWidth = originalIcon.getIconWidth();
        int originalHeight = originalIcon.getIconHeight();

        int targetWidth, targetHeight;

        if (originalWidth > originalHeight) {
            // 宽度较大，按照宽度进行等比例缩放
            targetWidth = baseSize;
            targetHeight = (int) ((double) originalHeight / originalWidth * baseSize);
        } else {
            // 高度较大，按照高度进行等比例缩放
            targetHeight = baseSize;
            targetWidth = (int) ((double) originalWidth / originalHeight * baseSize);
        }

        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return new ImageIcon(scaledImage);
    }


    // 使用JPanel的子类，重写paintComponent方法来绘制背景图片
    private static class ImagePanel extends JPanel {
        private final Image background;

        public ImagePanel(Image background) {
            this.background = background;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            // 启用抗锯齿
            RenderingHints hints = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHints(hints);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Image backgroundImage = (Image) getClientProperty("backgroundImage");
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private static JPanel createBackgroundPanel(String imagePath) {
        Image backgroundImage = new ImageIcon(DynamicLotteryApp.class.getResource(imagePath)).getImage();
        return new ImagePanel(backgroundImage);
    }

    private static void changeBackground(JPanel panel, String newImagePath) {
        Image newImage = new ImageIcon(DynamicLotteryApp.class.getResource(newImagePath)).getImage();

        // 直接设置背景图片
        panel.putClientProperty("backgroundImage", newImage);
        panel.revalidate();
        panel.repaint();
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
            if (!specialPrizeRadio.isSelected() && !firstPrizeRadio.isSelected() && !secondPrizeRadio.isSelected() && !thirdPrizeRadio.isSelected()) {
                JOptionPane.showMessageDialog(frame, "请选中要抽的奖项！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            specialPrizeRadio.setEnabled(false);
            firstPrizeRadio.setEnabled(false);
            secondPrizeRadio.setEnabled(false);
            thirdPrizeRadio.setEnabled(false);

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
            thirdPrizeRadio.setEnabled(true);

            isDrawing = false;
            drawButton.setEnabled(true);
            stopButton.setEnabled(false);
            clearButton.setEnabled(true);

            executorService.shutdown();
        }
    }

    private void updateLotteryList(int selectedPrize) {
        if (!currentLotteryList.isEmpty()) {
            currentLotteryList.clear();
        }

        switch (selectedPrize) {
            case 0:
            case 1:
                winnersList = drawer.drawPrize(specEmployeeMap, 1);
                currentLotteryList.add(String.format("%s %s", winnersList.get(0), specEmployeeMap.get(winnersList.get(0))));
                break;
            case 2:
            case 3:
                winnersList = drawer.drawPrize(normEmployeeMap, 10);
                for (int i = 0; i < 10; i++) {
                    currentLotteryList.add(String.format("%s %s", winnersList.get(i), normEmployeeMap.get(winnersList.get(i))));
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

    private void updatePriceImgPathList(int selectedPrize) {
        if (!currentPriceImgPathList.isEmpty()) {
            currentPriceImgPathList.clear();
        }
        String basePath = "/images/price/";
        switch (selectedPrize) {
            case 3:
                for (int i = 1; i <= 4; i++) {
                    String path = basePath + String.format("3.%d.jpg", i);
                    currentPriceImgPathList.add(path);
                }
                break;
            case 2:
                for (int i = 1; i <= 4; i++) {
                    String path = basePath + String.format("2.%d.jpg", i);
                    currentPriceImgPathList.add(path);
                }
                break;
            case 1:
                for (int i = 1; i <= 5; i++) {
                    String path = basePath + String.format("1.%d.jpg", i);
                    currentPriceImgPathList.add(path);
                }
                break;
            case 0:
                for (int i = 1; i <= 3; i++) {
                    String path = basePath + String.format("0.%d.jpg", i);
                    currentPriceImgPathList.add(path);
                }
                break;
            default:
                break;
        }
    }

    private void initPriceNameMap() {
        String[] allPriceList = new String[] {
             "usmile电动牙刷", "小米小爱触屏音箱", "泰国乳胶枕", "美的空气炸锅", // 三等奖
             "全自动咖啡机", "徕芬高速吹风机", "小米照片打印机", "不莱玫行李箱", // 二等奖
             "极米Play3投影仪", "Apple AirPods Pro", "GoPro运动相机", "戴森无线吸尘器", "Switch任天堂游戏机", //一等奖
             "Apple Watch Ultra", "iPhone 15", "iPad Pro" //特等奖
        };
        String basePath = "/images/price/";
        int index = 1;
        int category = 3;
        for (int i = 0; i < 4; i++) {
            String path = String.format("%s%d.%d.jpg", basePath, category, index);
            allPriceNameMap.put(path, allPriceList[i]);
            index++;
        }

        category--;
        index = 1;
        for (int i = 4; i < 8; i++) {
            String path = String.format("%s%d.%d.jpg", basePath, category, index);
            allPriceNameMap.put(path, allPriceList[i]);
            index++;
        }

        category--;
        index = 1;
        for (int i = 8; i < 13; i++) {
            String path = String.format("%s%d.%d.jpg", basePath, category, index);
            allPriceNameMap.put(path, allPriceList[i]);
            index++;
        }

        category--;
        index = 1;
        for (int i = 13; i < 16; i++) {
            String path = String.format("%s%d.%d.jpg", basePath, category, index);
            allPriceNameMap.put(path, allPriceList[i]);
            index++;
        }
    }

    private void showCurrentLotteryList() {
        namePanel.removeAll();

        int colCount = 2;  // 每行显示的列数

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 25, 10, 25); // 设置间隔

        for (int i = 0; i < currentLotteryList.size(); i++) {
            JLabel label = new JLabel(currentLotteryList.get(i));
            if (currentLotteryList.size() == 1) {
                label.setFont(new Font("微软雅黑", Font.PLAIN, 80));  // 设置字体大小为18
            } else {
                label.setFont(new Font("微软雅黑", Font.PLAIN, 50));  // 设置字体大小为18
            }
            label.setForeground(Color.white);
            namePanel.add(label, gbc);
            // 每 colCount 个元素换行
            if ((i + 1) % colCount == 0) {
                gbc.gridx = 0;
                gbc.gridy++;
            } else {
                gbc.gridx++;
            }
        }

        namePanel.revalidate();
        namePanel.repaint();
    }


    private void clearAndSaveWinners() {
        ListToText.exportListToFile(priceLabel.getText(), currentLotteryList);
        drawer.redeemPrize(winnersList);
        currentLotteryList.clear();
        namePanel.removeAll();
        namePanel.revalidate();
        namePanel.repaint();
        if (currentPrice == 3) {
            updateAndCheckIfPrizeDrawn(currentPrice, 7, thirdPrizeRadio, true);
        } else if (currentPrice == 2) {
            updateAndCheckIfPrizeDrawn(currentPrice, 3, secondPrizeRadio, false);
        } else if (currentPrice == 1) {
            updateAndCheckIfPrizeDrawn(currentPrice, 4, firstPrizeRadio, false);
        } else if (currentPrice == 0) {
            updateAndCheckIfPrizeDrawn(currentPrice, 2, specialPrizeRadio, false);
        }
    }

    private void updateAndCheckIfPrizeDrawn(int currentPrice, int maxIndex, JRadioButton prizeRadio, boolean checkEvenIndex) {
        if (currentPriceIndex < maxIndex) {
            currentPriceIndex++;
            if (!checkEvenIndex || currentPriceIndex % 2 == 0) {
                updateCurrentPrice(currentPrice, checkEvenIndex ? currentPriceIndex / 2 : currentPriceIndex);
            }
        } else {
            JOptionPane.showMessageDialog(frame, String.format("当前%s已抽完", getPrizeString(currentPrice)),
                    "提示", JOptionPane.WARNING_MESSAGE);
            priceLabel.setVisible(false);
            imageLabel.setVisible(false);
            prizeRadio.setEnabled(false);
        }
    }


    private int getSelectedPrize() {
        if (specialPrizeRadio.isSelected()) {
            return 0;
        } else if (firstPrizeRadio.isSelected()) {
            return 1;
        } else if (secondPrizeRadio.isSelected()) {
            return 2;
        } else if (thirdPrizeRadio.isSelected()) {
            return 3;
        } else {
            return -1;
        }
    }

    private String getPrizeString(int selectedPrize) {
         switch (selectedPrize) {
            case 3:
                return "三等奖";
            case 2:
                return "二等奖";
            case 1:
                return "一等奖";
            case 0:
                return "特等奖";
            default:
                return "";
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
