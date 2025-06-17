package igu.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WindowResizer extends MouseAdapter {
    private JFrame frame;
    private Point startPoint;
    private int cursor;
    private final int BORDER_WIDTH = 4; 
    private boolean resizing = false;
    private Rectangle startBounds;

    public WindowResizer(JFrame frame) {
        this.frame = frame;
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
    }

    public void addToComponent(Component component) {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }

    public boolean isResizing() {
        return resizing;
    }

    public void mousePressed(MouseEvent e) {
        startPoint = e.getLocationOnScreen();
        startBounds = frame.getBounds();
        cursor = getCursor(e);
        resizing = (cursor != Cursor.DEFAULT_CURSOR);

        if (resizing) {
            e.consume();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (resizing) {
            e.consume();
        }
        resizing = false;
        startPoint = null;
        startBounds = null;
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseMoved(MouseEvent e) {
        if (!resizing) {
            cursor = getCursor(e);
            frame.setCursor(Cursor.getPredefinedCursor(cursor));
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (startPoint != null && resizing && startBounds != null) {
            e.consume();

            Point currentPoint = e.getLocationOnScreen();
            int dx = currentPoint.x - startPoint.x;
            int dy = currentPoint.y - startPoint.y;

            Rectangle newBounds = new Rectangle(startBounds);

            switch (cursor) {
                case Cursor.N_RESIZE_CURSOR:
                    int newY = startBounds.y + dy;
                    int newHeight = startBounds.height - dy;
                    if (newHeight >= 600) { // Verificar tamaño mínimo
                        newBounds.y = newY;
                        newBounds.height = newHeight;
                    } else {
                        newBounds.y = startBounds.y + startBounds.height - 600;
                        newBounds.height = 600;
                    }
                    break;
                case Cursor.S_RESIZE_CURSOR:
                    newBounds.height = Math.max(600, startBounds.height + dy);
                    break;
                case Cursor.W_RESIZE_CURSOR:
                    int newX = startBounds.x + dx;
                    int newWidth = startBounds.width - dx;
                    if (newWidth >= 1000) {
                        newBounds.x = newX;
                        newBounds.width = newWidth;
                    } else {
                        newBounds.x = startBounds.x + startBounds.width - 1000;
                        newBounds.width = 1000;
                    }
                    break;
                case Cursor.E_RESIZE_CURSOR:
                    newBounds.width = Math.max(1000, startBounds.width + dx);
                    break;
                case Cursor.NW_RESIZE_CURSOR:
                    // Esquina noroeste - combinar lógica de N y W
                    int nwNewX = startBounds.x + dx;
                    int nwNewY = startBounds.y + dy;
                    int nwNewWidth = startBounds.width - dx;
                    int nwNewHeight = startBounds.height - dy;
                    
                    if (nwNewWidth >= 1000 && nwNewHeight >= 600) {
                        newBounds.x = nwNewX;
                        newBounds.y = nwNewY;
                        newBounds.width = nwNewWidth;
                        newBounds.height = nwNewHeight;
                    } else {
                        if (nwNewWidth < 1000) {
                            newBounds.x = startBounds.x + startBounds.width - 1000;
                            newBounds.width = 1000;
                        } else {
                            newBounds.x = nwNewX;
                            newBounds.width = nwNewWidth;
                        }
                        if (nwNewHeight < 600) {
                            newBounds.y = startBounds.y + startBounds.height - 600;
                            newBounds.height = 600;
                        } else {
                            newBounds.y = nwNewY;
                            newBounds.height = nwNewHeight;
                        }
                    }
                    break;
                case Cursor.NE_RESIZE_CURSOR:
                    int neNewY = startBounds.y + dy;
                    int neNewWidth = startBounds.width + dx;
                    int neNewHeight = startBounds.height - dy;
                    
                    newBounds.width = Math.max(1000, neNewWidth);
                    if (neNewHeight >= 600) {
                        newBounds.y = neNewY;
                        newBounds.height = neNewHeight;
                    } else {
                        newBounds.y = startBounds.y + startBounds.height - 600;
                        newBounds.height = 600;
                    }
                    break;
                case Cursor.SW_RESIZE_CURSOR:
                    int swNewX = startBounds.x + dx;
                    int swNewWidth = startBounds.width - dx;
                    int swNewHeight = startBounds.height + dy;
                    
                    newBounds.height = Math.max(600, swNewHeight);
                    if (swNewWidth >= 1000) {
                        newBounds.x = swNewX;
                        newBounds.width = swNewWidth;
                    } else {
                        newBounds.x = startBounds.x + startBounds.width - 1000;
                        newBounds.width = 1000;
                    }
                    break;
                case Cursor.SE_RESIZE_CURSOR:
                    newBounds.width = Math.max(1000, startBounds.width + dx);
                    newBounds.height = Math.max(600, startBounds.height + dy);
                    break;
            }
            
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            if (newBounds.x < 0) {
                newBounds.width += newBounds.x;
                newBounds.x = 0;
            }
            if (newBounds.y < 0) {
                newBounds.height += newBounds.y;
                newBounds.y = 0;
            }
            if (newBounds.x + newBounds.width > screenSize.width) {
                newBounds.width = screenSize.width - newBounds.x;
            }
            if (newBounds.y + newBounds.height > screenSize.height) {
                newBounds.height = screenSize.height - newBounds.y;
            }
            
            frame.setBounds(newBounds);
        }
    }
    
    private int getCursor(MouseEvent e) {
        Point windowPoint;
        if (e.getSource() == frame) {
            windowPoint = e.getPoint();
        } else {
            windowPoint = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), frame);
        }
        
        int x = windowPoint.x;
        int y = windowPoint.y;
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        boolean topBorder = y < BORDER_WIDTH; 
        
        if (x < BORDER_WIDTH && topBorder) return Cursor.NW_RESIZE_CURSOR;
        if (x > width - BORDER_WIDTH && topBorder) return Cursor.NE_RESIZE_CURSOR;
        if (x < BORDER_WIDTH && y > height - BORDER_WIDTH) return Cursor.SW_RESIZE_CURSOR;
        if (x > width - BORDER_WIDTH && y > height - BORDER_WIDTH) return Cursor.SE_RESIZE_CURSOR;
        if (x < BORDER_WIDTH) return Cursor.W_RESIZE_CURSOR;
        if (x > width - BORDER_WIDTH) return Cursor.E_RESIZE_CURSOR;
        if (topBorder) return Cursor.N_RESIZE_CURSOR;
        if (y > height - BORDER_WIDTH) return Cursor.S_RESIZE_CURSOR;
        
        return Cursor.DEFAULT_CURSOR;
    }
}
