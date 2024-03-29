package pl.suseu.chip8;

import processing.core.PApplet;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Random;

import static pl.suseu.chip8.UnsignedUtils.uint;

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

    private Screen screen;

    public Emulator(Screen screen) throws Exception {
        this.screen = screen;
//        new Thread(() -> {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            gfx[2] = true;
//            screen.redraw(gfx.clone());
//        }).start();

        initialize();
    }

    private void initialize() throws Exception {
        pc = 0x200; // Program counter always starts at 0x200 (512)

        for (int i = 0; i < Font.getFontset().length; i++) {
            memory[i] = Font.getFontset()[i];
        }
        byte[] binary = loadBinary("roms/space_invaders.ch8");
        for (int i = 0; i < binary.length; i++) {
            memory[i + 512] = binary[i];
        }

        new Thread(() -> {
            while (true) {
                long start = System.currentTimeMillis();
                if (soundTimer > 0)
                    soundTimer--;
                if (delayTimer > 0)
                    delayTimer--;

                for (int i = 0; i < keys.length; i++) {
                    keys[i] = (byte) screen.getKeys().getBuffer()[i];
                }

                emulateCycle();
                long stop = System.currentTimeMillis();
//                System.out.println("Cycle took " + (stop - start) + "ms. Now waiting...");
                try {
//                    Thread.sleep(1);
                    Thread.sleep(1000 / 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void emulateCycle() {
        opcode = (char) (uint(memory[pc]) << 8 | uint(memory[pc + 1]));
//        System.out.println("memory[pc] = " + memory[pc]);
//        System.out.println("memory[pc+1] = " + memory[pc + 1]);
//        System.out.println("Opcode: 0x" + Integer.toHexString(uint(opcode)));

        switch (opcode & 0xF000) {
            case 0x000: {
                switch (opcode & 0x00F) {
                    case 0x000: // 0x00E0: Clears the screen
                        Arrays.fill(gfx, false);
                        screen.redraw(gfx.clone());
                        pc += 2;
                        break;
                    case 0x00E: //0x000E: Returns from subroutine
                        pc = stack[sp];
                        sp--;
                        pc += 2;
                        break;
                }
                break;
            }

            // 0x1NNN: Jumps to address NNN.
            case 0x1000: {
                pc = opcode & 0x0FFF;
                break;
            }

            // 0x2NNN: Calls subroutine at NNN.
            case 0x2000: {
                sp++;
                stack[sp] = pc;
                pc = opcode & 0x0FFF;
                break;
            }

            //3f01
            // 0x3XNN: Skips the next instruction if VX equals NN. (Usually the next instruction is a jump to skip a code block)
            case 0x3000: {
                if (uint(V[opcode >>> 8 & 0xF]) == (opcode & 0xFF))
                    pc += 4;
                else
                    pc += 2;
                break;
            }

            //0x4XNN: Skips the next instruction if VX doesn't equal NN. (Usually the next instruction is a jump to skip a code block)
            case 0x4000: {
                if (V[opcode >>> 8 & 0xF] != (opcode & 0xFF))
                    pc += 4;
                else
                    pc += 2;
                break;
            }

            //0x5XY0: Skips the next instruction if VX equals VY. (Usually the next instruction is a jump to skip a code block)
            case 0x5000: {
                if (V[opcode >>> 8 & 0xF] == V[opcode >>> 4 & 0xF])
                    pc += 4;
                else
                    pc += 2;
                break;
            }

            //0x6XNN: Sets VX to NN.
            case 0x6000: {
                V[opcode >>> 8 & 0xF] = (byte) (opcode & 0xFF);
                pc += 2;
                break;
            }

            //0x7XNN: Adds NN to VX. (Carry flag is not changed)
            case 0x7000: {
                V[opcode >>> 8 & 0xF] += (byte) (opcode & 0xFF);
                pc += 2;
                break;
            }

            //0x8XY?
            case 0x8000: {
                byte x = (byte) ((opcode >>> 8) & 0xF);
                byte y = (byte) ((opcode >>> 4) & 0xF);

                switch (opcode & 0x000F) {
                    //0x8XY0: Sets VX to the value of VY.
                    case 0x0000: {
                        V[x] = V[y];
                        pc += 2;
                        break;
                    }

                    //0x8XY1: Sets VX to VX or VY. (Bitwise OR operation)
                    case 0x0001: {
                        V[x] = (byte) (V[x] | V[y]);
                        pc += 2;
                        break;
                    }

                    //0x8XY2: Sets VX to VX and VY. (Bitwise AND operation)
                    case 0x0002: {
                        V[x] = (byte) (V[x] & V[y]);
                        pc += 2;
                        break;
                    }

                    //0x8XY3: Sets VX to VX xor VY.
                    case 0x0003: {
                        V[x] = (byte) (V[x] ^ V[y]);
                        pc += 2;
                        break;
                    }

                    //0x8XY4: Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                    case 0x0004: {
                        int sum = uint(V[x]) + uint(V[y]);

                        if (sum > 255) // carry
                            V[0xF] = 1;
                        else // no carry
                            V[0xF] = 0;
                        V[x] = (byte) (sum & 0xFF);

                        pc += 2;
                        break;
                    }

                    //0x8XY5: VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                    case 0x0005: {
                        int difference = uint(V[x]) - uint(V[y]);
                        if (difference < 0)
                            V[0xF] = 0;
                        else
                            V[0xF] = 1;
                        V[x] = (byte) (difference & 0xFF);
                        pc += 2;
                        break;
                    }

                    //0x8XY6: Stores the least significant bit of VX in VF and then shifts VX to the right by 1.
                    case 0x0006: {
                        V[0xF] = (byte) (V[x] & 0b1);
                        V[x] = (byte) (uint(V[x]) >>> 1 & 0xFF);
                        pc += 2;
                        break;
                    }

                    //0x8XY7: Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                    case 0x0007: {
                        int difference = uint(V[y]) - uint(V[x]);
                        if (difference < 0)
                            V[0xF] = 0;
                        else
                            V[0xF] = 1;
                        V[x] = (byte) (difference & 0xFF);
                        pc += 2;
                        break;
                    }


                    //0x8XYE: Stores the most significant bit of VX in VF and then shifts VX to the left by 1.
                    case 0x000E: {
                        V[0xF] = (byte) (((uint(V[x]) & 0xFF) & 0b10000000) >>> 7); // 0x8000 = 0b1000000000000000
                        V[x] = (byte) (V[x] << 1 & 0xFF);
                        pc += 2;
                        break;
                    }

                    default:
                        screen.closeWindow();
                        System.err.println("Unknown opcode: 0x" + Integer.toHexString(uint(opcode)).toUpperCase());
                        System.exit(0);
                        break;
                }
                break;
            }

            //0x9XY0: Skips the next instruction if VX doesn't equal VY. (Usually the next instruction is a jump to skip a code block)
            case 0x9000: {
                byte x = (byte) ((opcode >>> 8) & 0xF);
                byte y = (byte) ((opcode >>> 4) & 0xF);
                if (V[x] != V[y])
                    pc += 4;
                else
                    pc += 2;
                break;
            }

            //0xANNN: Sets I to the address NNN.
            case 0xA000: {
                I = opcode & 0xFFF;
                pc += 2;
                break;
            }

            //0xBNNN: Jumps to the address NNN plus V0.
            case 0xB000: {
                pc = uint(V[0]) + (opcode & 0xFFF);
                break;
            }

            //0xCXNN: Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
            case 0xC000: {
                int x = (opcode >>> 8) & 0xF;
                int rand = (new Random().nextInt(255)) & (opcode & 0xFF);
                V[x] = (byte) (rand & 0xFF);
                pc += 2;
                break;
            }

            //0xDXYN: Draws a sprite at coordinate (VX, VY)
            case 0xD000: {
                int x = uint(V[(byte) ((opcode >>> 8) & 0xF)]);
                int y = uint(V[(byte) ((opcode >>> 4) & 0xF)]);
                int n = opcode & 0xF;

                V[0xF] = 0;

                for (int y2 = 0; y2 < n; y2++) {
                    byte sprite = memory[I + y2];
                    for (int x2 = 0; x2 < 8; x2++) {
                        boolean draw = (uint(sprite) & (0x80 >>> x2)) != 0;
                        if (!draw)
                            continue;

                        int index = (y + y2) * 64 + x + x2;
                        if(index >= 2048)
                            break;
                        boolean before = gfx[index];
                        gfx[index] ^= true;
                        boolean after = gfx[index];

                        if (before != after) {
                            V[0xF] = 1;
                        }
                    }
                }
                screen.redraw(gfx.clone());
                pc += 2;
                break;
            }


            //0xEX??
            case 0xE000: {
                int x = (byte) ((opcode >>> 8) & 0xF);
                switch (opcode & 0xFF) {
                    //0xEX9E: Skips the next instruction if the key stored in VX is pressed.
                    case 0x009E: {
                        if (keys[V[x]] == 1)
                            pc += 4;
                        else
                            pc += 2;
                        break;
                    }

                    //0xEXA1: Skips the next instruction if the key stored in VX isn't pressed.
                    case 0x00A1: {
                        if (keys[V[x]] == 1)
                            pc += 2;
                        else
                            pc += 4;
                        break;
                    }

                    default:
                        screen.closeWindow();
                        System.err.println("Unknown opcode: 0x" + Integer.toHexString(uint(opcode)).toUpperCase());
                        System.exit(0);
                        break;
                }
                break;
            }

            //0xFX??
            case 0xF000: {
                int x = (opcode >>> 8) & 0xF;
                switch (opcode & 0xFF) {
                    //0xFX07: Sets VX to the value of the delay timer.
                    case 0x0007: {
                        V[x] = (byte) (delayTimer & 0xFF);
                        pc += 2;
                        break;
                    }

                    //0xFX0A: A key press is awaited, and then stored in VX. (Blocking Operation. All instruction halted until next key event)
                    case 0x000A: {
                        for (byte key : keys) {
                            if (key == 1) {
                                V[x] = key;
                                pc += 2;
                                break;
                            }
                        }
                        //not incrementing pc to wait for key press
                        break;
                    }

                    //0xFX15: Sets the delay timer to VX.
                    case 0x0015: {
                        delayTimer = uint(V[x]);
                        pc += 2;
                        break;
                    }

                    //0xFX18: Sets the sound timer to VX.
                    case 0x0018: {
                        soundTimer = uint(V[x]);
                        pc += 2;
                        break;
                    }

                    //0xFX1E: Adds VX to I.
                    case 0x001E: {
                        I += uint(V[x]);
                        pc += 2;
                        break;
                    }

                    //0xFX29: Sets I to the location of the sprite for the character in VX.
                    // Characters 0-F (in hexadecimal) are represented by a 4x5 font.
                    case 0x0029: {
                        I = uint(V[x]) * 5;
                        pc += 2;
                        break;
                    }


                    //0xFX33: too long to write here lol https://en.wikipedia.org/wiki/CHIP-8
                    case 0x0033: {
                        int vx = uint(V[x]);

                        int ones = (vx % 10) & 0xFF;
                        int tens = ((vx / 10) % 10) & 0xFF;
                        int hundreds = ((vx / 100) % 10) & 0xFF;

                        memory[I] = (byte) hundreds;
                        memory[I + 1] = (byte) tens;
                        memory[I + 2] = (byte) ones;

                        pc += 2;
                        break;
                    }

                    //0xFX55: Stores V0 to VX (including VX) in memory starting at address I.
                    // The offset from I is increased by 1 for each value written, but I itself is left unmodified.
                    case 0x0055: {
                        for (int i = 0; i <= x; i++) {
                            memory[I + i] = V[i];
                        }
                        pc += 2;
                        break;
                    }

                    //0xFX65: Fills V0 to VX (including VX) with values from memory starting at address I.
                    // The offset from I is increased by 1 for each value written, but I itself is left unmodified.
                    case 0x0065: {
                        for (int i = 0; i <= x; i++) {
                            V[i] = memory[I + i];
                        }
                        pc += 2;
                        break;
                    }
                }
                break;
            }

            default:
                screen.closeWindow();
                System.err.println("Unknown opcode: 0x" + Integer.toHexString(uint(opcode)).toUpperCase());
                System.exit(0);
                break;
        }
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
        PApplet.main("pl.suseu.chip8.Screen");
    }
}
