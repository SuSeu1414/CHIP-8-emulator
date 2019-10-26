package pl.suseu.chip8;

public class UnsignedUtils {

    public static int uint(boolean b) {
        return b ? 1 : 0;
    }

    public static int uint(byte b) {
        return b & 0xFF;
    }

    public static int uint(short s) {
        return s & 0xFFFF;
    }

    public static int uint(char c) { // char can be used as unsigned short
        return c;
    }
}
