package gui;

import emulator.GameManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by xeniu on 02.04.2017.
 */
public class GUI extends JFrame implements Observer {
    private Canvas pCanvas;
    private JPanel pEditor,pOutputLeft, pRegisterOutput, pEditorHexOutput, pEditorHexControls;
    private JTextArea taEditor, taOutput;
    private JButton btExecuteCode, btEmulateOneCycle, btPlayPause, btHexSet;
    private JMenuBar toolbar;
    private JMenu menuFile, menuGame, menuEmulator;
    private JMenuItem miExit, miLoadGame, miNewGame, miOpenEditor, miClearDisplay, miOptions;
    private TableModelMemory tablemodelMemory;
    private TableRendererMemory tableRendererMemory;
    private JTable tableMemory;
    private JScrollPane scrollpaneMemory;
    private JLabel laHexFrom, laHexTo;
    private JTextField tfHexFrom, tfHexTo;

    private GameManager gameManager;

    public GUI() throws HeadlessException {
        initComponents();
        gameManager = new GameManager(this.pCanvas);
        gameManager.getEmulator().addObserver(this);
    }

    private void initComponents() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(500, 500);
        this.setLocationRelativeTo(null);

        // initialization
        pCanvas = new Canvas();

        pOutputLeft=new JPanel(new BorderLayout());

        pRegisterOutput = new JPanel(new BorderLayout());
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
        miOpenEditor = new JMenuItem("Open Editor");
        miClearDisplay = new JMenuItem("Clear display");
        miOptions = new JMenuItem("Options --> reset keybindings");
        btPlayPause = new JButton("Start");

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
                loadGame();
            }
        });

        miNewGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loadGame();
                startGame();
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
                gameManager.emulateOpcode(0x00E0);
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

        // setter
        menuEmulator.add(miOptions);
        menuEmulator.add(miOpenEditor);
        menuEmulator.add(miClearDisplay);
        menuGame.add(miNewGame);
        menuGame.add(miLoadGame);
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
        pOutputLeft.add(pEditorHexOutput,BorderLayout.CENTER);

        pOutputLeft.add(taOutput, BorderLayout.EAST);

        this.add(pOutputLeft, BorderLayout.WEST);

        // after all is done set visible true
        this.setVisible(true);
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
        try {
            gameManager.resetKeybindings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void emulateOneCycle() {
        gameManager.emulateOneCycle();
    }

    private void executeCode() {
        String[] code = taEditor.getText().split("\n");
        for (String codeline : code) {
            try {
                int opcode = Integer.decode(codeline);
                gameManager.emulateOpcode(opcode);
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
    public void startGame() {
        gameManager.startGame();
        btPlayPause.setText("Playing |>");
    }

    private void loadGame() {
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
            }
        }
    }

    /**
     * Should only be used for testing ...
     *
     * @param filepath
     */
    public void loadGame(String filepath) {
        try {
            gameManager.loadGame(filepath);
            tablemodelMemory.setMemory(gameManager.getMemory());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot load file", "File IO exception", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public Canvas getCanvasPanel() {
        return pCanvas;
    }

    @Override
    public void update(Observable observable, Object o) {
        // set the simple registers and stuff
        taOutput.setText(gameManager.getOutputString());
        // output the memory
        tablemodelMemory.setMemory(gameManager.getMemory());
        tablemodelMemory.fireTableDataChanged();
    }

    public void updateMemory(int[] memory) {
        gameManager.setMemory(memory);
    }
}
