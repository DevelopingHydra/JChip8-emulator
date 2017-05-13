package emulator;

import gui.Canvas;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by xeniu on 02.04.2017.
 */
public class GameManager implements Runnable {
    private chip8 emulator;
    private Canvas canvas;
    private HashMap<String, String> keybindings;
    private int sleepTime;
    private boolean isSuspended;
    private Thread ownThread;

    private final File configFile = new File(
            System.getProperty("user.dir") + File.separator + "src" +
                    File.separator + "config" + File.separator + "keybinding.conf");

    public GameManager(Canvas canvas) {
        this.canvas = canvas;
        this.emulator = new chip8(canvas);
        this.keybindings = new HashMap<>();
        this.sleepTime = 1000 / 60; // 60Hz
        this.isSuspended = false;

        try {
            loadKeybindings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * later change parameter
     *
     * @param keyEvent
     */
    public void onKeyPressed(KeyEvent keyEvent) {
        char key = keyEvent.getKeyChar();
        if (this.keybindings.containsKey(key + "")) {
            char emulatedKey = this.keybindings.get(key + "").charAt(0);
            emulator.setKeyPressed(emulatedKey);
        }
    }

    public void startGame() {
        System.out.println("STARTING GAME");
        // reset the emulator state
        emulator.reset();
        this.isSuspended = false;
        ownThread = new Thread(this);
        ownThread.start();
    }

    @Override
    public void run() {
        while (!ownThread.isInterrupted()) {
            try {
                emulateOneCycle();
                try {
                    ownThread.sleep(this.sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (this) {
                    while (this.isSuspended) {
                        wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void emulateOneCycle() {
        emulator.emulateCycle();
        if (emulator.isDrawFlagSet()) {
            canvas.setCanvas(emulator.getCanvas());
        }
    }

    public void emulateOpcode(int opcode) {
        emulator.setOpcode(opcode);
        emulator.executeOpCode();
        if (emulator.isDrawFlagSet()) {
            canvas.setCanvas(emulator.getCanvas());
        }
    }

    public String getOutputString() {
        return emulator.outputRegisters();
    }

    public void loadGame(String file) throws IOException {
        emulator.reset();
        emulator.loadGame(file);
        loadKeybindings();
    }

    public void loadKeybindings() throws IOException {
        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream(configFile);
        defaultProps.load(in);
        in.close();

        for (String key : defaultProps.stringPropertyNames()) {
            String value = defaultProps.getProperty(key);
            this.keybindings.put(key, value);
        }
    }

    public void resetKeybindings() throws IOException {
        /*
            Keypad                   Keyboard
            +-+-+-+-+                +-+-+-+-+
            |1|2|3|C|                |1|2|3|4|
            +-+-+-+-+                +-+-+-+-+
            |4|5|6|D|                |Q|W|E|R|
            +-+-+-+-+       =>       +-+-+-+-+
            |7|8|9|E|                |A|S|D|F|
            +-+-+-+-+                +-+-+-+-+
            |A|0|B|F|                |Y|X|C|V|
            +-+-+-+-+                +-+-+-+-+
         */

        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream(configFile);
        defaultProps.load(in);
        in.close();

//        Properties applicationProps = new Properties(defaultProps);
        defaultProps.setProperty("1", "1");
        defaultProps.setProperty("2", "2");
        defaultProps.setProperty("3", "3");
        defaultProps.setProperty("q", "4");
        defaultProps.setProperty("w", "5");
        defaultProps.setProperty("e", "6");
        defaultProps.setProperty("a", "7");
        defaultProps.setProperty("s", "8");
        defaultProps.setProperty("d", "9");
        defaultProps.setProperty("x", "0");
        defaultProps.setProperty("y", "A");
        defaultProps.setProperty("c", "B");
        defaultProps.setProperty("4", "C");
        defaultProps.setProperty("r", "D");
        defaultProps.setProperty("f", "E");
        defaultProps.setProperty("v", "F");

        FileOutputStream out = new FileOutputStream(configFile);
        defaultProps.store(out, "---Keybindings---");
        out.close();
    }

    public chip8 getEmulator() {
        return emulator;
    }

    public int[] getMemory() {
        return emulator.getMemory();
    }

    public void setMemory(int[] memory) {
        emulator.setMemory(memory);
    }

    public synchronized void play() {
        this.isSuspended = false;
        notify();
    }

    public void pause() {
        this.isSuspended = true;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public boolean hasStarted() {
        return ownThread != null && !ownThread.isInterrupted();
    }
}