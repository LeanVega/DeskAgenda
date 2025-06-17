package igu.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ComponentMover extends MouseAdapter {
    private JFrame frame;
    private Point clickPoint;
    private boolean dragging = false;

    public ComponentMover(JFrame frame, Component component) {
        this.frame = frame;
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }

    public void mousePressed(MouseEvent e) {
        clickPoint = e.getPoint();
        dragging = true;
    }

    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }

    public void mouseDragged(MouseEvent e) {
        if (clickPoint != null && dragging) {
            Point currentLocation = frame.getLocation();
            int newX = currentLocation.x + e.getX() - clickPoint.x;
            int newY = currentLocation.y + e.getY() - clickPoint.y;

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            newX = Math.max(0, Math.min(newX, screenSize.width - frame.getWidth()));
            newY = Math.max(0, Math.min(newY, screenSize.height - frame.getHeight()));

            frame.setLocation(newX, newY);
        }
    }
}
