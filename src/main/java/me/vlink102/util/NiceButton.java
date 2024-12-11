package me.vlink102.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CompletableFuture;

public class NiceButton extends JButton {

    private Color baseColor;
    private Color hoverColor;
    private Color clickColor;

    public NiceButton(String text, Color baseColor, Color hoverColor, Color clickColor, Runnable runnable) {
        super(text); // Set button text

        // Store colors
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        this.clickColor = clickColor;

        // Set defaults
        //setForeground(Color.WHITE); // Text color
        //setFocusPainted(false); // Remove focus border
        //setBorderPainted(false); // Remove border
        //setContentAreaFilled(false); // Remove default background


        // Add mouse listeners for hover and click effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                //setBackground(hoverColor);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //setBackground(baseColor);
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //setBackground(clickColor);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                CompletableFuture.runAsync(runnable);
                //setBackground(hoverColor);
                repaint();
            }
        });

        //setBackground(baseColor);
    }
/*
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Enable anti-aliasing for smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a rounded rectangle with gradient background
        int width = getWidth();
        int height = getHeight();
        GradientPaint gradient = new GradientPaint(0, 0, getBackground().brighter(), 0, height, getBackground().darker());
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width, height, 20, 20); // Rounded corners

        // Draw button text
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(getText());
        int textHeight = fm.getHeight();
        g2.drawString(getText(), (width - textWidth) / 2, (height + textHeight / 2) / 2 - 2);

        g2.dispose(); // Dispose of graphics context
    }*/

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 40); // Default button size
    }
}