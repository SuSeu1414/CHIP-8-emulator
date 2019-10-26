package pl.suseu.chip8;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Screen extends Frame implements WindowListener, KeyListener {

    private boolean closed = false;
    private boolean redraw = true;
    private Emulator emulator;
    private Keys keys = new Keys();

    public Screen() {
        addWindowListener(this);
        setTitle("CHIP-8 Emulator by Szymon");
        setSize(640 + 80, 320 + 80);
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        if (!redraw) {
            return;
        }
        System.out.println("Drawing!");
        redraw = false;

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.BLACK);
        g2.drawRect(40, 40, getWidth() - 80, getHeight() - 80);

        int x = 0;
        int y = 0;
        for (int i = 0; i < emulator.getGfx().length; i++) {
            if(closed)
                return;

            Color c = emulator.getGfx()[i] ? Color.BLACK : Color.LIGHT_GRAY;
            drawPoint(g2, x, y, c);

            x++;
            if(x > 63) {
                x = 0;
                y++;
            }
        }
    }

    public void drawPoint(Graphics2D g2, int x, int y, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(10));
        g2.drawLine(10 * x + 45, 10 * y + 45, 10 * x + 45, 10 * y + 45);
    }

    public void closeWindow() {
        closed = true;
        this.dispose();
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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys.keyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.keyReleased(e.getKeyCode());
    }

    public Keys getKeys() {
        return keys;
    }

    public void redraw() {
        this.redraw = true;
    }
}
