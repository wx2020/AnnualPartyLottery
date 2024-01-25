package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;

import static com.sun.java.accessibility.util.AWTEventMonitor.addComponentListener;

public class MainForm {
    private JPanel mainForm;
    private JButton button1;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainForm");

        //1.把图片添加到标签里（把标签的大小设为和图片大小相同），把标签放在分层面板的最底层；
        ImageIcon bg = new ImageIcon(MainForm.class.getResource("/images/bg.png"));
        JLabel label = new JLabel(bg) {
            @Override
            public void paint(Graphics g) {
                Image img = bg.getImage();
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
            }
        };
        frame.getLayeredPane().add(label,new Integer(Integer.MIN_VALUE));
        //2.把窗口面板设为内容面板并设为透明、流动布局。
        JPanel pan = (JPanel) frame.getContentPane();
        pan.setOpaque(false);
        frame.setTitle("年会抽奖系统"); //标题
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {  //监测整个框架大小是否改变
                label.setSize(frame.getSize());
                label.repaint();  //重绘，会自动调用paint方法
            }
        });
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("d.width = " + d.width + ", d.height = " + d.height);
        frame.setSize(d.width, d.height); //全屏
        label.setSize(frame.getSize());  // label大小等于frame的大小

        setButton(frame.getLayeredPane());  // 添加按钮到分层面板

        ImageIcon imageIcon = new ImageIcon(MainForm.class.getResource("/images/fih_icon.png"));
        frame.setIconImage(imageIcon.getImage());
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void setButton(JLayeredPane layeredPane) {
        JButton button = new JButton("Click Me");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(layeredPane, "Button Clicked!");
            }
        });

        // 设置按钮的位置和大小
        button.setBounds(50, 100, 150, 30);

        // 将按钮添加到分层面板的上层
        layeredPane.add(button, JLayeredPane.POPUP_LAYER);
    }

    private static void setButton11(JPanel panel) {
        JButton button = new JButton("Click Me");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(panel, "Button Clicked!");
            }
        });

        // 设置按钮的位置和大小
        button.setBounds(50, 50, 150, 30);

        // 将按钮添加到内容面板
        panel.add(button);
    }

    private static void setButton1(JPanel pan) {
        JButton button1 = new JButton("一等奖");
        button1.setBackground(Color.BLUE); // 设置按钮背景颜色
        button1.setForeground(Color.WHITE); // 设置按钮前景（文本）颜色
        button1.setBounds(100, 200, 50, 50);

        JButton button2 = new JButton("二等奖");
        button2.setBackground(Color.GREEN); // 设置按钮背景颜色
        button2.setForeground(Color.BLACK); // 设置按钮前景（文本）颜色
        button1.setBounds(100, 400, 50, 50);

        pan.add(button1);
        pan.add(button2);
    }
}
