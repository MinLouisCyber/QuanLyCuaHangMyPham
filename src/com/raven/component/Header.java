package com.raven.component;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Header extends JPanel {

    private JLabel title;

    public Header() {
        setOpaque(false);
        setPreferredSize(new Dimension(0, 60));

        title = new JLabel("QUẢN LÍ CỬA HÀNG MỸ PHẨM", new ImageIcon(getClass().getResource("/com/raven/icon/logo.png")), JLabel.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0, 20, 0, 0));

        setLayout(new BorderLayout());
        add(title, BorderLayout.WEST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(5, 5, getWidth() - 10, getHeight(), 20, 20);

        // Gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(72, 61, 255),
                                             getWidth(), getHeight(), new Color(123, 31, 162));
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        super.paintComponent(g);
    }
}
