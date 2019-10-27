package pl.suseu.chip8;

import processing.core.PApplet;
import processing.event.KeyEvent;

import java.util.Arrays;

public class Screen extends PApplet {

    private boolean closed = false;
    private boolean redraw = true;
    private Emulator emulator;
    private Keys keys = new Keys();
    private boolean[] gfx = new boolean[64 * 32];

    @Override
    public void settings() {
        size(640 + 80, 320 + 80);
        try {
            new Emulator(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw() {
        if (!redraw) {
            return;
        }
        redraw = false;

        background(200);
        stroke(0);
        strokeWeight(2);
        rect(40, 40, width - 80 + 1, height - 80 + 1);

        int x = 0;
        int y = 0;
        for (int i = 0; i < gfx.length; i++) {
            if(closed)
                return;

            int color = gfx[i] ? 0 : 200;
            drawPoint(x, y, color);

            x++;
            if(x > 63) {
                x = 0;
                y++;
            }
        }
    }


    public void drawPoint(int x, int y, int color) {
        strokeWeight(10);
        stroke(color);
        rect(10 * x + 45, 10 * y + 45, 1, 1);
    }

    public void closeWindow() {
        closed = true;
        this.dispose();
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

    public void redraw(boolean[] gfx) {
        this.gfx = gfx;
        this.redraw = true;
    }
}
