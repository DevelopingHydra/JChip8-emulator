package emulator;

import exception.EmulatorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Observable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by xeniu on 02.04.2017.
 */
public class Chip8 extends Observable {

    private int[] registersV;
    private int pointerI;
    private int timerDelay, timerSound;
    private int[] memory;
    private int opcode;
    private int[] stack;
    private int stackpointer;
    private char[] keysPressed;
    private int programcounter;
    private int[][] canvas; // holds only 0 or 1

    private boolean isDrawFlagSet, isSoundFlagSet;

    private final int[] fontset = new int[]{
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80 // F
    };

    public Chip8() {
        reset();
        loadFontsInMemory();
    }

    private void loadFontsInMemory() {
        for (int i = 0; i < fontset.length; i++) {
            memory[i] = (fontset[i] >> 4);
        }
    }

    public void emulateCycle() throws EmulatorException {
        opcode = (memory[programcounter] << 8) | memory[programcounter + 1]; // first bit-shift the first by 8 to the right and then OR both

        // now we have execute the opcode
        executeOpCode();
    }

    public void executeOpCode() throws EmulatorException {
        // the first hex digit (4 bits) shows us what to do
        // the next 3 hex digits (12 bits) give us information like variables, values, ...
        // to get the first hex digit we AND the opcode with the hex mask 0xF000
        // to get the last 3 digits we AND with the mask 0x0FFF

        boolean doIncreaseProgramCounter = true;

//        System.out.println("parsing\t" + String.format("%02X", opcode));
        // common variables
        int _x, _y, _nn;
        boolean isKeyPressed = false;

        isDrawFlagSet = false;
        isSoundFlagSet = false;

        _nn = opcode & 0x00FF;
        _x = (opcode & 0x0F00) >> 8;
        _y = (opcode & 0x00F0) >> 4;

        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode & 0x0FFF) {
                    case 0x00E0:
                        // works
                        this.canvas = new int[32][64];
                        this.isDrawFlagSet = true;
                        break;
                    case 0x00EE:
                        // todo test
                        // return from a subroutine
                        // first set the program counter to the correct position
                        // then decrease the stack pointer
                        programcounter = stack[stackpointer - 1];
                        stackpointer--;
                        // after all that we do not want the program counter to increase by 2, because we jumped to the exact position
                        doIncreaseProgramCounter = false;
                        break;
                    default:
                        // todo test
                        // 0NNN
                        // this code will be ignored
                        break;
//                        throw new UnsupportedOperationException("Unknown opcode:\t0NNN");
                }
                break;
            case 0x1000:
                // todo test
                // there is only one option --> 1NNN
                // jumps to address NNN
                programcounter = opcode & 0x0FFF;
                doIncreaseProgramCounter = false;
                break;
            case 0x2000:
                // todo test
                // there is only one option --> 2NNN
                // calls subroutine at NNN
                stack[stackpointer] = programcounter + 2;
                stackpointer++;
                programcounter = opcode & 0x0FFF;
                doIncreaseProgramCounter = false;
                break;
            case 0x3000:
                // todo test
                // 3XNN
                // skips the next instruction if VX equals NN

                // _nn = opcode & 0x00FF;
                if (registersV[_x] == _nn) {
                    programcounter += 2;
                }
                break;
            case 0x4000:
                // todo test
                // 4XNN
                // skips the next instruction if VX does not equal NN

                // _nn = opcode & 0x00FF;
                if (registersV[_x] != _nn) {
                    programcounter += 2;
                }
                break;
            case 0x5000:
                // todo test
                // 5XY0
                // skips the next instruction if VX equals VY
                if (registersV[_x] == registersV[_y]) {
                    programcounter += 2;
                }
                break;
            case 0x6000:
                // works
                // 6XNN
                // sets VX to NN

                // _nn = opcode & 0x00FF;
                registersV[_x] = _nn;
                break;
            case 0x7000:
                // todo test
                // 7XNN
                // Adds NN to VX
                // todo is here a carry?

//                registersV[_x] += _nn;
                registersV[_x] = (registersV[_x] + _nn) & 0xFF; // only care for the 8 bit
                break;
            case 0x8000:
                switch (opcode & 0x000F) {
                    case 0x0000:
                        // todo test
                        // 8XY0
                        // Sets VX to VY
                        registersV[_x] = registersV[_y];
                        break;
                    case 0x0001:
                        // todo test
                        // 8XY1
                        // Sets VX to VX OR VY
                        // VF is reset to 0
                        registersV[_x] = registersV[_x] | registersV[_y];
                        registersV[registersV.length - 1] = 0;
                        break;
                    case 0x0002:
                        // todo test
                        // 8XY2
                        // Sets VX to VX AND VY
                        // VF is reset to 0
                        registersV[_x] = registersV[_x] & registersV[_y];
                        registersV[registersV.length - 1] = 0;
                        break;
                    case 0x0003:
                        // todo test
                        // 8XY3
                        // Sets VX to VX XOR VY
                        // VF is reset to 0


                        registersV[_x] = registersV[_x] ^ registersV[_y];
                        registersV[registersV.length - 1] = 0;
                        break;
                    case 0x0004:
                        // todo test
                        // 8XY4
                        // Adds VY to VX
                        // VF is set to 1 if there is a carry, otherwise 0


                        if (registersV[_x] + registersV[_y] > 255) {
//                            V[_x] = 255;
                            registersV[registersV.length - 1] = 1;
                        } else {
                            registersV[registersV.length - 1] = 0;
                        }
                        registersV[_x] += registersV[_y];
                        break;
                    case 0x0005:
                        // todo test
                        // 8XY5
                        // VY is subtracted from VX
                        // VF is set to 0 when there is a borrow, otherwise 1


                        if (registersV[_x] <= registersV[_y]) {
                            // todo dont know how to subtract more than there is... setting it to 0 for now
//                            V[_x] = 0;
                            registersV[registersV.length - 1] = 0;
                        } else {
                            registersV[registersV.length - 1] = 1;
                        }
                        registersV[_x] -= registersV[_y];
                        break;
                    case 0x0006:
                        // todo test
                        // 8XY6
                        // Sets VX right by one
                        // VF is set to the value of the least significant bit of VX before the shift

                        // useless
                        registersV[registersV.length - 1] = registersV[_x] & 1;
                        registersV[_x] = registersV[_x] >> 1;
                        break;
                    case 0x0007:
                        // todo test
                        // 8XY7
                        // Sets VX to VY minus VX
                        // VF is set to 0 when there is a borrow, otherwise 1


                        if (registersV[_x] >= registersV[_y]) {
                            // todo dont know how to subtract more than there is... setting it to 0 for now
//                            V[_x] = 0;
                            registersV[registersV.length - 1] = 0;
                        } else {
                            registersV[registersV.length - 1] = 1;
                        }
                        registersV[_x] = registersV[_y] - registersV[_x];
                        break;
                    case 0x000E:
                        // todo test
                        // 0x8XYE
                        // Shifts VX left by one
                        // VF is set to the value of the least significant bit of VX before the shift

                        // useless
                        registersV[registersV.length - 1] = registersV[_x] & 1;
                        registersV[_x] = registersV[_x] << 1;
                        break;
                    default:
//                        throw new UnsupportedOperationException("Unknown opcode:\t" + String.format("%02X", opcode));
                        throw new EmulatorException("Unkown opcode:\t" + String.format("%02X", opcode));
                }
                break;
            case 0x9000:
                // todo test
                // 9XY0
                // Skips the next instruction if VX does not equal VY


                if (registersV[_x] != registersV[_y]) {
                    programcounter += 2;
                }
                break;
            case 0xA000:
                // todo test
                // ANNN
                // Sets I to the address NNN
                pointerI = opcode & 0x0FFF;
                break;
            case 0xB000:
                // todo test
                // BNNN
                // Jumps to the address NNN plus V0
                programcounter = registersV[0] + (opcode & 0x0FFF);
                break;
            case 0xC000:
                // todo test
                // CXNN
                // Sets VX to the result of a bitwise AND operation on a random number (0 - 255)

                // _nn = opcode & 0x00FF;
                registersV[_x] = ThreadLocalRandom.current().nextInt(0, 256) & _nn;
                break;
            case 0xD000:
                // todo works... i think
                // DXYN
                // Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels.
                // Each row of 8 pixels is read as bit-coded starting from memory location I;
                // I value doesn’t change after the execution of this instruction.
                // VF is set to 1 if any screen pixels are flipped from set to unset when the sprite is drawn, and to 0 if that doesn’t happen
//                _x = registersV[(opcode & 0x0F00) >> 8];
//                _y = registersV[(opcode & 0x00F0) >> 4];
                int xCoord = registersV[_x];
                int yCoord = registersV[_y];
                int height = opcode & 0x000F;
                int pixel;

                // reset V[F]
                registersV[registersV.length - 1] = 0;

//                System.out.println("drawing at " + xCoord + " <-> " + yCoord);
                // loop for each row
                for (int line = 0; line < height; line++) {
                    pixel = memory[pointerI + line];
                    // loop through the 8 bits in this line
                    for (int col = 0; col < 8; col++) {
                        // current bit
                        int b = ((pixel >>> (8 - col - 1)) & 1);
//                        int b = pixel & (0x80 >> col);

//                        if (xCoord + col >= canvas[line].length) {
//                            System.out.println("out of right space\t" + xCoord + " + " + col + " >= " + canvas[line].length);
//                        }
                        if (b != 0 && yCoord + line < canvas.length && xCoord + col < canvas[line].length) {
                            // there is something to draw :)
                            // now check if there is a 1 one the current position we want to draw
                            if (canvas[yCoord + line][xCoord + col] == 1) {
                                // set the flag
                                registersV[registersV.length - 1] = 1;
//                                System.out.println("drawing on occupied tiles[x: "+(xCoord+col)+", y: "+(yCoord+line)+"] is a 1");
                            }
                            // draw by XORing the position
//                            canvas[xCoord + col][yCoord + line] ^= 1;
                            canvas[yCoord + line][xCoord + col] ^= 1;
//                            System.out.println("drawing at " + (xCoord + col) + " - " + (yCoord + line));
                        }
                    }
                }

                isDrawFlagSet = true;
                break;
            case 0xE000:
                switch (opcode & 0x000F) {
                    case 0x000E:
                        // todo test
                        // EX9E
                        // Skips the next instruction if the key stored in VX is pressed
                        if (keysPressed[registersV[_x]] != 0) {
                            programcounter += 2;
                        }
                        break;
                    case 0x0001:
                        // todo test
                        // EXA1
                        // Skips the next instruction if the key stored in VX is not pressed
                        if (keysPressed[registersV[_x]] == 0) {
                            programcounter += 2;
                        }
                        break;
                    case 0xE0A0:
                        // this opcode is not supported, but present in some games
                        // we don´t want an exception thrown and therefore just do nothing
                        break;
                    default:
                        throw new EmulatorException("Unknown opcode:\t" + String.format("%02X", opcode));
                }
                break;
            case 0xF000:
                switch (opcode & 0x00FF) {
                    case 0x0007:
                        // todo test
                        // FX07
                        // Sets V[x] to the value of the delay timer

                        registersV[_x] = timerDelay;
                        break;
                    case 0x000A:
                        // todo test
                        // FX0A
                        // A key press is awaited, and then stored in VX (Blocking operation. All instructions halted until next key event)
                        _x = (opcode & 0xF00) >> 8;
                        for (int i = 0; i < keysPressed.length; i++) {
                            if (keysPressed[i] == 1) {
                                registersV[_x] = i;
                                isKeyPressed = true;
                            }
                        }
                        if (!isKeyPressed) {
                            doIncreaseProgramCounter = false;
                        }
                        break;
                    case 0x0015:
                        // todo test
                        // FX15
                        // Sets the delay timer to VX

                        timerDelay = registersV[_x];
                        break;
                    case 0x0018:
                        // todo test
                        // FX18
                        // Sets the sound timer to VX

                        timerSound = registersV[_x];
                        break;
                    case 0x001E:
                        // todo test
                        // FX1E
                        // Adds VX to I

                        pointerI += registersV[_x];
                        break;
                    case 0x0029:
                        // works
                        // FX29
                        // Sets I to the location of the sprite for the character in VX
                        // Characters 0-F (in hex) are represented by a 4x5 font

                        pointerI = registersV[_x] * 5;
                        break;
                    case 0x0033:
                        // todo test
                        // FX33
                        // Stores the binary-coded decimal representation of VX,
                        // with the most significant of three digits at the address in I, the middle digit at I plus 1,
                        // and the least significant digit at I plus 2
                        // (In other words, take the decimal representation of VX,
                        // place the hundreds digit in memory at location in I, the tens digit at location I+1,
                        // and the ones digit at location I+2.)

                        // todo wrong cast!
                        memory[pointerI] = (registersV[_x] / 100);
                        memory[pointerI + 1] = ((registersV[_x] / 10) % 10);
                        memory[pointerI + 2] = ((registersV[_x] % 100) % 10);
                        break;
                    case 0x0055:
                        // todo test
                        // FX55
                        // Stores V0 to VX (including VX) in memory starting at address I

                        for (int i = 0; i <= _x; i++) {
                            memory[pointerI + i] = registersV[i];
                        }
                        break;
                    case 0x0065:
                        // todo test
                        // FX65
                        // Fills V0 to VX (including VX) with values from memory starting at address I

                        // every V is 8 bit long
                        for (int i = 0; i <= _x; i++) {
                            // read 8 bit into this register
                            registersV[i] = memory[pointerI + i];
                        }
                        break;
                    default:
//                        throw new UnsupportedOperationException("Unknown opcode:\t" + String.format("%02X", opcode));
                        throw new EmulatorException("Unkown opcode:\t" + String.format("%02X", opcode));
                }
                break;
            default:
//                throw new UnsupportedOperationException("Unknown opcode:\t" + String.format("%02X", opcode));
                throw new EmulatorException("Unkown opcode:\t" + String.format("%02X", opcode));
        }

        // now maybe increase the program counter by 2 to get to the next opcode
        if (doIncreaseProgramCounter) {
            programcounter += 2;
        }

        // manage timers
        if (timerDelay > 0) {
            timerDelay--;
        }

        if (timerSound > 0) {
            if (timerSound == 1) {
                System.err.println("BEEP");
//                Toolkit.getDefaultToolkit().beep();
                isSoundFlagSet = true;
            }
            timerSound--;
        }

        // notify observers
        setChanged();
        notifyObservers();
    }

    public void setKeyPressed(char key) {
//        System.out.println("key pressed:\t" + key);
        // converts the char to string and then to int :)
        this.keysPressed[Integer.parseInt(key + "", 16)] = 1;
    }

    public void setKeyReleased(char key) {
//        System.out.println("key released:\t" + key);
        // converts the char to string and then to int :)
        this.keysPressed[Integer.parseInt(key + "", 16)] = 0;
    }

    public void loadGame(String filepath) throws IOException, EmulatorException {
        if (programcounter < 0x200) {
            throw new EmulatorException("Game loaded befor 0x200");
        }

        //    System.out.println("Loading game: " + filepath);
        byte[] data = Files.readAllBytes(Paths.get(filepath));
//        for (int i = 0; i < data.length; i++) {
//            System.out.println(String.format("%02X", data[i]));
//        }

        // feed the data into the memory
        for (int i = 0; i < data.length; i++) {
            memory[programcounter] = data[i] & 0xff;
            programcounter++;
        }
        // reste pc
        programcounter = 0x200;
        //     System.out.println("Loaded game successfully");

        // notify observers
        setChanged();
        notifyObservers();
    }

    public void reset() {
        this.registersV = new int[16];
        this.memory = new int[4096];
        this.stack = new int[16];
        this.keysPressed = new char[16];
        this.canvas = new int[32][64];
        this.timerDelay = 0;
        this.timerSound = 0;
        this.opcode = 0;
        this.stackpointer = 0;
        this.programcounter = 0x200;
        this.pointerI = 0;
        this.isDrawFlagSet = false;

        // load fonts
        loadFontsInMemory();

        // notify observers
        setChanged();
        notifyObservers();
    }

    public void resetExceptMemory() {
        int[] tmpMemory = this.memory;
        this.reset();
        this.memory = tmpMemory;
    }

    public boolean isDrawFlagSet() {
        return isDrawFlagSet;
    }

    public boolean isSoundFlagSet() {
        return isSoundFlagSet;
    }

    public int[][] getCanvas() {
        return canvas;
    }

    public int[] getMemory() {
        return memory;
    }

    /**
     * todo remove in beta for testing/ide only
     *
     * @return
     */
    public String registersToString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("opcode --> %02X\n", opcode));

        sb.append(String.format("pc --> %02X\n", programcounter));

        sb.append("~ Vx ~\n");
        for (int i = 0; i < registersV.length; i += 2) {
            sb.append(String.format("V[%1X] --> %02X\tV[%1X] --> %02X\n", i, registersV[i], (i + 1), registersV[i + 1]));
        }

        sb.append("~ I ~\n");
        sb.append(String.format("I --> %02X\n", pointerI));

        sb.append("~ stack ~\n");
        for (int i = 0; i < stack.length / 2; i += 2) {
            sb.append(String.format("V[%d] --> %02X\tV[%d] --> %02X\n", i, stack[i], (i + 1), stack[i + 1]));
        }
        sb.append("sp --> ").append(stackpointer).append("\n");

        return sb.toString();
    }

    public void setOpcode(int code) {
        this.opcode = code;
    }

    public void setMemory(int[] memory) {
        this.memory = memory;
    }

    /**
     * todo remove in beta, because was replaced with Integer.parseInt(key, 16)
     *
     * @param key
     * @return
     */
    private int keyCharToInt(char key) {
        try {
            return Integer.parseInt(key + "");
        } catch (java.lang.NumberFormatException e) {
            switch ((key + "").toUpperCase().charAt(0)) {
                case 'A':
                    return 10;
                case 'B':
                    return 11;
                case 'C':
                    return 12;
                case 'D':
                    return 13;
                case 'E':
                    return 14;
                case 'F':
                    return 15;
                default:
//                    System.err.println("Key not recognised!");
                    throw new java.lang.NumberFormatException("Key not recognised");
            }
        }
    }

}
