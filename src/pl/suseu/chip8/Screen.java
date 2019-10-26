package pl.suseu.chip8;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Screen extends Frame implements WindowListener {

    public Screen() {
        addWindowListener(this);
        setTitle("CHIP-8 Emulator by Szymon");
        setSize(640 + 80, 320 + 80);
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.BLACK);
        g2.drawRect(40, 40, getWidth() - 80, getHeight() - 80);
        drawPoint(g2, 1, 2, Color.BLACK);
    }

    public void drawPoint(Graphics2D g2, int x, int y, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(10));
        g2.drawLine(10 * x + 45, 10 * y + 45, 10 * x + 45, 10 * y + 45);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
