package gui;

import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;

public class AnimatedLogo extends JPanel {
    private final ImageIcon logo;
    private float opacity = 0.0f;
    private Timer fadeTimer;

    
    public AnimatedLogo(ImageIcon logo) {
        this.logo = logo;
        setOpaque(false);
        
        // Create fade-in animation
        fadeTimer = new Timer(50, e -> {
            opacity = Math.min(1.0f, opacity + 0.1f);
            repaint();
            if (opacity >= 1.0f) {
                fadeTimer.stop();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Set opacity for fade effect
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        
        // Draw the logo centered
        int x = (getWidth() - logo.getIconWidth()) / 2;
        int y = (getHeight() - logo.getIconHeight()) / 2;
        logo.paintIcon(this, g2d, x, y);
        
        g2d.dispose();
    }
    
    public void startAnimation() {
        opacity = 0.0f;
        fadeTimer.start();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(logo.getIconWidth(), logo.getIconHeight());
    }
}