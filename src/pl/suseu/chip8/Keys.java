package pl.suseu.chip8;

import java.util.Arrays;

public class Keys {

    private int[] buffer = new int[16];
    private int[] ids = new int[256];

    public Keys() {
        Arrays.fill(ids, -1);

        ids['1'] = 1;
        ids['2'] = 2;
        ids['3'] = 3;
        ids['Q'] = 4;
        ids['W'] = 5;
        ids['E'] = 6;
        ids['A'] = 7;
        ids['S'] = 8;
        ids['D'] = 9;
        ids['Z'] = 0xA;
        ids['X'] = 0;
        ids['C'] = 0xB;
        ids['4'] = 0xC;
        ids['R'] = 0xD;
        ids['F'] = 0xE;
        ids['V'] = 0xF;
    }

    public void keyPressed(int keyCode){
        if(ids[keyCode] != -1)
            buffer[ids[keyCode]] = 1;
    }

    public void keyReleased(int keyCode){
        if(ids[keyCode] != -1)
            buffer[ids[keyCode]] = 0;
    }

    public int[] getBuffer() {
        return buffer;
    }
}
