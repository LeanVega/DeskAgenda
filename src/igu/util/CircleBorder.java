package igu.util;

import javax.swing.border.Border;
import java.awt.*;

public class CircleBorder implements Border {
    private Color color;

    public CircleBorder() {
        this(new Color(255, 152, 0));
    }

    public CircleBorder(Color color) {
        this.color = color;
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(2, 2, 2, 2);
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (color.getAlpha() > 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x, y, width - 1, height - 1);
            g2.dispose();
        }
    }
}
