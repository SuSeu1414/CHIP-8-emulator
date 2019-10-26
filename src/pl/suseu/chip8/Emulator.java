package pl.suseu.chip8;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static pl.suseu.chip8.UnsignedUtils.*;

public class Emulator {

    private char opcode; // current opcode, 2 bytes long
    private byte[] memory = new byte[4096]; // 4K memory
    private byte[] V = new byte[16]; // CPU registers, V[15] is 'carry flag'
    private int I; // index register, value between 0x000 - 0xfff
    private int pc; // program counter, value between 0x000 - 0xfff
    private boolean[] gfx = new boolean[64 * 32]; // screen
    private int delayTimer;
    private int soundTimer;
    private int[] stack = new int[16];
    private int sp; // stack pointer
    private byte[] keys = new byte[16]; // HEX based keypad

    public Emulator() throws Exception{
        initialize();
    }

    private void initialize() throws Exception{
        pc = 0x200; // Program counter always starts at 0x200 (512)

        //TODO: Load fontset

        byte[] binary = loadBinary("roms/Pong.c8");
        for (int i = 0; i < binary.length; i++) {
            memory[i+512] = binary[i];
        }
    }

    private void emulateCycle(){

    }

    private byte[] loadBinary(String filename) throws Exception {
        System.out.println("Loading " + filename + " ...");
        DataInputStream in = new DataInputStream(new FileInputStream(filename));
        byte[] bytes = new byte[in.available()];
        int loadedBytes = in.read(bytes);
        System.out.println("Loaded " + loadedBytes + " bytes.");
        return bytes;
    }

    public static void main(String[] args) throws Exception {
        new Emulator();
    }
}
