package gui;

import dal.DAL;
import emulator.GameManager;
import exception.EmulatorException;
import org.omg.CORBA.INTERNAL;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by xeniu on 02.04.2017.
 */
public class GUI extends JFrame implements Observer {

    private Canvas pCanvas;
    private JPanel pEditor, pOutputLeft, pEditorHexOutput, pEditorHexControls, pSpeed;
    private JTextArea taEditor, taOutput;
    private JButton btExecuteCode, btEmulateOneCycle, btPlayPause, btHexSet;
    private JMenuBar toolbar;
    private JMenu menuFile, menuGame, menuEmulator;
    private JMenuItem miExit, miLoadGame, miNewGame, miClearEmulator, miOpenEditor, miClearDisplay, miOptions;
    private TableModelMemory tablemodelMemory;
    private TableRendererMemory tableRendererMemory;
    private JTable tableMemory;
    private JScrollPane scrollpaneMemory;
    private JLabel laHexFrom, laHexTo, laSpeed, laSpeedOutput;
    private JTextField tfHexFrom, tfHexTo;
    private JSlider sliderSpeed;

    private GameManager gameManager;

    public GUI() {
        initComponents();

        try {
            gameManager = new GameManager();
            gameManager.getEmulator().addObserver(this);
        } catch (IOException e) {
            System.err.println("Init failed");
            System.exit(14);
        }

        try {
            loadSettings();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to load settings!", "File error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void initComponents() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(1000, 500);
        this.setLocationRelativeTo(null);

        // initialization
        pCanvas = new Canvas();

        pOutputLeft = new JPanel(new BorderLayout());

        taOutput = new JTextArea();

        pEditor = new JPanel(new BorderLayout());
        taEditor = new JTextArea();

        pEditorHexOutput = new JPanel(new BorderLayout());
        btExecuteCode = new JButton("Execute code");
        btEmulateOneCycle = new JButton("Emulate one cycle");
        tableRendererMemory = new TableRendererMemory();
        tablemodelMemory = new TableModelMemory(this);
        tableMemory = new JTable(tablemodelMemory);
        scrollpaneMemory = new JScrollPane(tableMemory);

        pEditorHexControls = new JPanel(new GridLayout(1, 3));
        laHexFrom = new JLabel("From:");
        laHexTo = new JLabel("To:");
        tfHexFrom = new JTextField("0x200");
        tfHexTo = new JTextField("0x300");
        btHexSet = new JButton("Set range");

        toolbar = new JMenuBar();
        menuFile = new JMenu("File");
        menuGame = new JMenu("Game");
        menuEmulator = new JMenu("Emulator");
        miExit = new JMenuItem("Exit");
        miLoadGame = new JMenuItem("Load Game");
        miNewGame = new JMenuItem("New Game");
        miClearEmulator = new JMenuItem("Clear emulator");
        miOpenEditor = new JMenuItem("Open Editor");
        miClearDisplay = new JMenuItem("Clear display");
        miOptions = new JMenuItem("Options");
        btPlayPause = new JButton("Start");

        pSpeed = new JPanel(new GridLayout());
        sliderSpeed = new JSlider();
        laSpeedOutput = new JLabel("60 Hz");
        laSpeed = new JLabel("Refresh speed");

        // listeners
        miExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(1);
            }
        });

        miLoadGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onLoadGame();
            }
        });

        miNewGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onLoadGame();
                onStartGame();
            }
        });

        miClearEmulator.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                gameManager.getEmulator().reset();
            }
        });

        miOpenEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openEditor();
            }
        });

        btExecuteCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                executeCode();
            }
        });

        btEmulateOneCycle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                emulateOneCycle();
            }
        });

        miClearDisplay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onEmulateOneCycle();
            }
        });

        miOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onOpenOptions();
            }
        });

        btPlayPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onPlayPause();
            }
        });

        pCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                gameManager.onKeyReleased(keyEvent);
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                gameManager.onKeyPressed(keyEvent);
            }
        });

        pCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                pCanvas.requestFocus();
            }
        });

        pCanvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                super.focusGained(focusEvent);
                pCanvas.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                super.focusLost(focusEvent);
                pCanvas.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {
                super.windowOpened(windowEvent);
                pCanvas.requestFocus();
            }
        });

        btHexSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                tablemodelMemory.setRange(Integer.decode(tfHexFrom.getText()), Integer.decode(tfHexTo.getText()));
            }
        });

        sliderSpeed.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (gameManager != null) {
                    // gamemanager not yet initialized ...
                    laSpeedOutput.setText(sliderSpeed.getValue() + " Hz");
                    gameManager.setSpeedInHz(sliderSpeed.getValue());
                }
            }
        });

        // settings
        pEditor.setVisible(false);
        pEditor.setBorder(BorderFactory.createTitledBorder("One opcode per line"));

        pOutputLeft.setVisible(false);
        pOutputLeft.setBorder(BorderFactory.createTitledBorder("Output"));

        tableMemory.getColumnModel().getColumn(0).setPreferredWidth(40);
        for (int i = 1; i < tableMemory.getColumnModel().getColumnCount(); i++) {
            tableMemory.getColumnModel().getColumn(i).setPreferredWidth(10);
        }

        tableMemory.setDefaultRenderer(Object.class, tableRendererMemory);

        btPlayPause.setFocusable(false);

        sliderSpeed.setMinimum(10);
        sliderSpeed.setMaximum(60 * 5); // max 5 times original speed
        sliderSpeed.setValue(60);
        sliderSpeed.setMajorTickSpacing(50);
        sliderSpeed.setPaintTicks(true);
        sliderSpeed.setPaintLabels(true);

        // setter
        menuEmulator.add(miOptions);
        menuEmulator.add(miOpenEditor);
        menuEmulator.add(miClearDisplay);
        menuGame.add(miNewGame);
        menuGame.add(miLoadGame);
        menuGame.add(miClearEmulator);
        menuFile.add(miExit);
        toolbar.add(menuFile);
        toolbar.add(menuGame);
        toolbar.add(menuEmulator);
        toolbar.add(btPlayPause);
        this.setJMenuBar(toolbar);

        this.add(pCanvas, BorderLayout.CENTER);

        pEditor.add(taEditor, BorderLayout.CENTER);
        pEditor.add(btExecuteCode, BorderLayout.SOUTH);
        pEditor.add(btEmulateOneCycle, BorderLayout.NORTH);
        this.add(pEditor, BorderLayout.EAST);

        scrollpaneMemory.setViewportView(tableMemory);
        pEditorHexOutput.add(scrollpaneMemory, BorderLayout.CENTER);
        pEditorHexControls.add(laHexFrom);
        pEditorHexControls.add(tfHexFrom);
        pEditorHexControls.add(laHexTo);
        pEditorHexControls.add(tfHexTo);
        pEditorHexControls.add(btHexSet);
        pEditorHexOutput.add(pEditorHexControls, BorderLayout.SOUTH);
        pOutputLeft.add(pEditorHexOutput, BorderLayout.CENTER);

        pOutputLeft.add(taOutput, BorderLayout.EAST);

        pSpeed.add(laSpeed);
        pSpeed.add(sliderSpeed);
        pSpeed.add(laSpeedOutput);
        this.add(pSpeed, BorderLayout.SOUTH);

        this.add(pOutputLeft, BorderLayout.WEST);

        // after all is done set visible true
        this.setVisible(true);
    }

    private void onEmulateOneCycle() {

        try {
            gameManager.emulateOpcode(0x00E0);
        } catch (EmulatorException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Emulator error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void onPlayPause() {
        boolean isRunning = !gameManager.isSuspended();
        if (!gameManager.hasStarted()) {
            gameManager.startGame();
            btPlayPause.setText("Playing |>");
        } else if (isRunning) {
            gameManager.pause();
            btPlayPause.setText("Paused ||");
        } else {
            gameManager.play();
            btPlayPause.setText("Playing |>");
        }
    }

    private void onOpenOptions() {
        // pause the game
        gameManager.pause();

        // show settings dialog
        Settings guiSettings = new Settings();
        guiSettings.setModal(true);
        guiSettings.setVisible(true);

        if (guiSettings.isSaved()) {
            // make the settings effective
            try {
                loadSettings();
                this.pCanvas.repaint();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Unable to load settings!", "File error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }

        // resume game
        gameManager.play();
    }

    private void loadSettings() throws IOException {
        HashMap<String, String> hmSettings = DAL.getInstance().loadSettings();
        for (Map.Entry<String, String> entry : hmSettings.entrySet()) {
            switch (entry.getKey()) {
                case "color_background":
                    this.pCanvas.setBgColor(Color.decode("#" + entry.getValue()));
                    break;
                case "color_foreground":
                    this.pCanvas.setDrawColor(Color.decode("#" + entry.getValue()));
                    break;
                case "speed":
                    gameManager.setSpeedInHz(Integer.parseInt(entry.getValue()));
                    this.laSpeedOutput.setText(entry.getValue());
                    this.sliderSpeed.setValue(Integer.parseInt(entry.getValue()));
                    break;
                case "mode_eyesore":
                    this.pCanvas.setEyesoreMode(Boolean.parseBoolean(entry.getValue()));
                    break;
                default:
                    System.out.println("Unknown setting found in file");
            }
        }
    }

    private void emulateOneCycle() {
        try {
            gameManager.emulateOneCycle();
        } catch (EmulatorException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Emulator error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void executeCode() {
        String[] code = taEditor.getText().split("\n");
        for (String codeline : code) {
            try {
                int opcode = Integer.decode(codeline);
                try {
                    gameManager.emulateOpcode(opcode);
                } catch (EmulatorException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Emulator error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            } catch (java.lang.NumberFormatException e) {
                System.out.println("Unable to decode opcode:\t" + codeline);
            }
        }
    }

    private void openEditor() {
        if (pEditor.isVisible()) {
            pEditor.setVisible(false);
            pOutputLeft.setVisible(false);
            miOpenEditor.setText("Open Editor");
        } else {
            pEditor.setVisible(true);
            pOutputLeft.setVisible(true);
            miOpenEditor.setText("Close Editor");
            // adjust the size
            if (this.getWidth() < 1000 && Toolkit.getDefaultToolkit().getScreenSize().getWidth() > 1000) {
                this.setSize(1000, this.getHeight());
                this.setLocationRelativeTo(null);
            }
        }
    }

    /**
     * only public during testing
     */
    public void onStartGame() {
        gameManager.startGame();
        btPlayPause.setText("Playing |>");
    }

    private void onLoadGame() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "c8games"));
        int retValue = fc.showDialog(null, "Load game");
        if (retValue == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                gameManager.loadGame(file.getAbsolutePath());
                gameManager.pause();
                btPlayPause.setText("Start Game |>");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Cannot load file", "File IO exception", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (EmulatorException ex) {
                printEmulatorErrorMsg(ex);
            }
        }
    }

    private void printEmulatorErrorMsg(EmulatorException ex) {
        JOptionPane.showMessageDialog(null, ex.getMessage(), "Emulator exception", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    /**
     * Should only be used for testing ... todo remove in beta
     *
     * @param filepath
     */
    public void onLoadGame(String filepath) {
        try {
            gameManager.loadGame(filepath);
            tablemodelMemory.setMemory(gameManager.getEmulator().getMemory());
        } catch (EmulatorException ex) {
            printEmulatorErrorMsg(ex);
        } catch (IOException ex2) {
            JOptionPane.showMessageDialog(null, "Cannot load file", "File IO exception", JOptionPane.ERROR_MESSAGE);
            ex2.printStackTrace();
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        // check if we have to draw something
        if (gameManager.getEmulator().isDrawFlagSet()) {
            this.pCanvas.setCanvas(gameManager.getEmulator().getCanvas());
        }

        // check if we have to make a sound
        if (gameManager.getEmulator().isSoundFlagSet()) {
            Toolkit.getDefaultToolkit().beep();
        }

        // set the simple registers and stuff
        taOutput.setText(gameManager.getOutputString());
        // output the memory
        tablemodelMemory.setMemory(gameManager.getEmulator().getMemory());
        tablemodelMemory.fireTableDataChanged();
    }

    public void updateMemory(int[] memory) {
        gameManager.getEmulator().setMemory(memory);
    }
}
