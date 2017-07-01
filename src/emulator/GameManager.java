package emulator;

import dal.DAL;
import exception.EmulatorException;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xeniu on 02.04.2017.
 */
public class GameManager implements Runnable {

    private Chip8 emulator;
    // private Canvas canvas;
    private HashMap<String, String> keybindings;
    private int sleepTime;
    private boolean isSuspended;
    private Thread ownThread;

//    private final File configFile = new File(
//            System.getProperty("user.dir") + File.separator + "src"
//            + File.separator + "config" + File.separator + "keybinding.conf");

    public GameManager() throws IOException {
        this.emulator = new Chip8();
        this.keybindings = new HashMap<>();
        this.sleepTime = 1000 / 60; // 60Hz
        this.isSuspended = false;


        loadKeybindings();
    }

    /**
     * later change parameter
     *
     * @param keyEvent
     */
    public void onKeyPressed(KeyEvent keyEvent) {
        char key = keyEvent.getKeyChar();
        if (this.keybindings.containsValue(key + "")) {
            // find the key to this pressed key
            for (Map.Entry<String, String> entry : this.keybindings.entrySet()) {
                if (entry.getValue().equals(key + "")) {
                    emulator.setKeyPressed(entry.getKey().charAt(0));
                    break;
                }
            }
        }
//        if (this.keybindings.containsKey(key + "")) {
//            char emulatedKey = this.keybindings.get(key + "").charAt(0);
//            emulator.setKeyPressed(emulatedKey);
//        }
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        char key = keyEvent.getKeyChar();
        if (this.keybindings.containsValue(key + "")) {
            // find the key to this pressed key
            for (Map.Entry<String, String> entry : this.keybindings.entrySet()) {
                if (entry.getValue().equals(key + "")) {
                    emulator.setKeyReleased(entry.getKey().charAt(0));
                    break;
                }
            }
        }
//        if (this.keybindings.containsKey(key + "")) {
//            char emulatedKey = this.keybindings.get(key + "").charAt(0);
//            emulator.setKeyReleased(emulatedKey);
//        }
    }

    public void startGame() {
        //    System.out.println("STARTING GAME");
        // reset the emulator state
        emulator.resetExceptMemory();
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
            } catch (EmulatorException e) {
                handleEmulatorException(e);
            }
        }
    }

    public void emulateOneCycle() throws EmulatorException {
        emulator.emulateCycle();
//        if (emulator.isDrawFlagSet()) {
//            canvas.setCanvas(emulator.getCanvas());
//        }
    }

    public void emulateOpcode(int opcode) throws EmulatorException {
        emulator.setOpcode(opcode);
        emulator.executeOpCode();
//        if (emulator.isDrawFlagSet()) {
//            canvas.setCanvas(emulator.getCanvas());
//        }
    }

    public String getOutputString() {
        return emulator.registersToString();
    }

    public void loadGame(String file) throws IOException, EmulatorException {
        emulator.reset();
        emulator.loadGame(file);
        loadKeybindings();
    }

    private void loadKeybindings() throws IOException {
        this.keybindings = DAL.getInstance().loadKeybindings();
    }

    public void resetKeybindings() throws IOException {
        DAL.getInstance().resetKeybindings();
    }

    public Chip8 getEmulator() {
        return emulator;
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

    private void handleEmulatorException(EmulatorException e) {
        System.err.println("Game exception:\n---\n" + e.getMessage() + "\n---\n");
    }

    public boolean hasStarted() {
        return ownThread != null && !ownThread.isInterrupted();
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setSpeedInHz(int speed) {
        setSleepTime(1000 / speed);
    }
}
