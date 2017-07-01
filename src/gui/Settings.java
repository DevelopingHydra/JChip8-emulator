package gui;

import dal.DAL;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xeniu on 21.05.2017.
 */
public class Settings extends JDialog {
    private JLabel lbSpeed, lbColorBackground, lbColorForeground, lbEyeSoreMode, lbSound, lbKeybindings;
    private JTextField tfSpeed;
    private JCheckBox cbEyeSoreMode;
    private JPanel paBottom, paGridsettingsGame;
    private JButton btCancel, btSave, btReset, btOpenColorDialogBackground, btOpenColorDialogForeground, btOpenKeybindingsDialog;
    private JComboBox<String> cbSounds;

    private ColorDialog diaColor;
    private KeybindingsDialog diaKeybindings;

    private boolean isSaved;
    private Color cBackground, cForeground;

    public Settings() throws HeadlessException {
        initComponents();
        initSettings();

        isSaved = false;
    }


    private void initComponents() {
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout(0, 2));
        this.setSize(500, 250);
        this.setLocationRelativeTo(null);

        // components
        lbSpeed = new JLabel("Default speed");
        lbColorBackground = new JLabel("Background color");
        lbColorForeground = new JLabel("Foregound color");
        lbEyeSoreMode = new JLabel("Eyesore mode");
        lbSound = new JLabel("Sound");
        lbKeybindings = new JLabel("Change Keybindings");

        tfSpeed = new JTextField();

        cbEyeSoreMode = new JCheckBox();

        cbSounds = new JComboBox<>();

        paGridsettingsGame = new JPanel(new GridLayout(0, 2));
        paBottom = new JPanel(new GridLayout(0, 3));

        btCancel = new JButton("Cancel");
        btSave = new JButton("Save");
        btReset = new JButton("Reset to default");
        btOpenColorDialogBackground = new JButton("select color");
        btOpenColorDialogForeground = new JButton("select color");
        btOpenKeybindingsDialog = new JButton("change keybindings");

        diaColor = new ColorDialog();
        diaKeybindings = new KeybindingsDialog();

        // listeners
        btCancel.addActionListener(actionEvent -> onCancel());
        btSave.addActionListener(actionEvent -> onSave());
        btReset.addActionListener(actionEvent -> onReset());
        btOpenColorDialogBackground.addActionListener(actionEvent -> {
            diaColor.openColorChooser();
            if (diaColor.isToBeSaved()) {
                btOpenColorDialogBackground.setBackground(diaColor.getChosenColor());
                cBackground = diaColor.getChosenColor();
            }
        });
        btOpenColorDialogForeground.addActionListener(actionEvent -> {
            diaColor.openColorChooser();
            if (diaColor.isToBeSaved()) {
                btOpenColorDialogForeground.setBackground(diaColor.getChosenColor());
                cForeground = diaColor.getChosenColor();
            }
        });
        btOpenKeybindingsDialog.addActionListener(actionEvent -> {
            diaKeybindings.openKeybindingsDialog();
            if (diaKeybindings.isToBeSaved()) {
                try {
                    DAL.getInstance().saveKeybindings(diaKeybindings.getKeybindings());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Unable to save settings to file!", "File error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });

        // settings
        btOpenColorDialogForeground.setForeground(Color.WHITE);

        // adding
        paBottom.add(btCancel);
        paBottom.add(btReset);
        paBottom.add(btSave);

        paGridsettingsGame.add(lbSound);
        paGridsettingsGame.add(cbSounds);
        paGridsettingsGame.add(lbSpeed);
        paGridsettingsGame.add(tfSpeed);
        paGridsettingsGame.add(lbColorBackground);
        paGridsettingsGame.add(btOpenColorDialogBackground);
        paGridsettingsGame.add(lbColorForeground);
        paGridsettingsGame.add(btOpenColorDialogForeground);
        paGridsettingsGame.add(lbEyeSoreMode);
        paGridsettingsGame.add(cbEyeSoreMode);
        paGridsettingsGame.add(lbKeybindings);
        paGridsettingsGame.add(btOpenKeybindingsDialog);

        this.add(paGridsettingsGame, BorderLayout.CENTER);
        this.add(paBottom, BorderLayout.SOUTH);
    }


    private void onReset() {
        isSaved = true;
        try {
            DAL.getInstance().resetKeybindings();
            DAL.getInstance().resetSettings();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to reset settings!", "File error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        this.setVisible(false);
    }

    private void onSave() {
        isSaved = true;

        HashMap<String, String> hmSettings = new HashMap<>();
        if (cBackground != null) {
            hmSettings.put("color_background", Integer.toHexString(cBackground.getRGB()).substring(2).toUpperCase());
        }
        if (cForeground != null) {
            hmSettings.put("color_foreground", Integer.toHexString(cForeground.getRGB()).substring(2).toUpperCase());
        }
        hmSettings.put("speed", tfSpeed.getText());
        if (cbEyeSoreMode.isSelected()) {
            hmSettings.put("mode_eyesore", "true");
        } else {
            hmSettings.put("mode_eyesore", "false");
        }
        hmSettings.put("sound", (String) cbSounds.getSelectedItem());

        try {
            DAL.getInstance().saveSettings(hmSettings);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to save settings to file!", "File error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        this.setVisible(false);
    }

    private void onCancel() {
        isSaved = false;
        this.setVisible(false);
    }

    private void initSettings() {
        // load sounds
        String[] sounds = DAL.getInstance().listAvailableSoundFiles();
        for (String s : sounds) {
            cbSounds.addItem(s);
        }

        try {
            HashMap<String, String> hmSettings = DAL.getInstance().loadSettings();
            for (Map.Entry<String, String> entry : hmSettings.entrySet()) {
                switch (entry.getKey()) {
                    case "color_background":
                        cBackground = Color.decode("#" + entry.getValue());
                        btOpenColorDialogBackground.setBackground(cBackground);
                        break;
                    case "color_foreground":
                        cForeground = Color.decode("#" + entry.getValue());
                        btOpenColorDialogForeground.setBackground(cForeground);
                        break;
                    case "speed":
                        tfSpeed.setText(entry.getValue());
                        break;
                    case "mode_eyesore":
                        cbEyeSoreMode.setSelected(Boolean.parseBoolean(entry.getValue()));
                        break;
                    case "sound":
                        for (int i = 0; i < sounds.length; i++) {
                            if (entry.getValue().equals(sounds[i])) {
                                cbSounds.setSelectedIndex(i);
                                break;
                            }
                        }
                        break;
                    default:
                        System.out.println("Unknown setting found in file");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to load settings file!", "File error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public boolean isSaved() {
        return isSaved;
    }

    public JTextField getTfSpeed() {
        return tfSpeed;
    }

    public Color getcBackground() {
        return cBackground;
    }

    public Color getcForeground() {
        return cForeground;
    }
}

class ColorDialog extends JDialog {

    private JColorChooser cc;
    private JPanel paBottom;
    private JButton btCancel, btSave;

    private Color chosenColor;
    private boolean isToBeSaved;

    public ColorDialog() {
        initComponents();
    }

    private void initComponents() {
        this.setSize(400, 300);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);
        this.setModal(true);

        // other components
        paBottom = new JPanel(new GridLayout(0, 2));
        btCancel = new JButton("Cancel");
        btSave = new JButton("Save");

        cc = new JColorChooser();

        // listeners
        btSave.addActionListener(actionEvent -> {
            this.chosenColor = cc.getColor();
            this.isToBeSaved = true;
            this.setVisible(false);
        });
        btCancel.addActionListener(actionEvent -> {
            this.isToBeSaved = false;
            this.setVisible(false);
        });

        // Adding
        paBottom.add(btCancel);
        paBottom.add(btSave);

        this.add(paBottom, BorderLayout.SOUTH);
        this.add(cc, BorderLayout.CENTER);
    }

    public void openColorChooser() {
        this.isToBeSaved = false;
        this.setVisible(true);
    }

    public Color getChosenColor() {
        return chosenColor;
    }

    public boolean isToBeSaved() {
        return isToBeSaved;
    }
}

class KeybindingsDialog extends JDialog {

    private JPanel paBottom, paCenter, paReference, paKeybindings;
    private JButton btCancel, btSave, btReset;

    private HashMap<String, String> keybindings;
    private boolean isToBeSaved;

    public KeybindingsDialog() {
        initComponents();
    }

    private void initComponents() {
        this.setSize(400, 300);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);
        this.setModal(true);

        // other components
        paBottom = new JPanel(new GridLayout(0, 3));
        paCenter = new JPanel(new GridLayout(0, 2));
        paReference = new JPanel(new GridLayout(4, 4));
        paKeybindings = new JPanel(new GridLayout(4, 4));

        btCancel = new JButton("Cancel");
        btSave = new JButton("Save");
        btReset = new JButton("Reset to default");

        // options
        paReference.setBorder(BorderFactory.createTitledBorder("CHIP-8 Keyboard"));
        paKeybindings.setBorder(BorderFactory.createTitledBorder("Emulator Keybindings"));

        // listeners
        btSave.addActionListener(actionEvent -> {
            this.isToBeSaved = true;
            this.setVisible(false);
        });
        btCancel.addActionListener(actionEvent -> {
            this.isToBeSaved = false;
            this.setVisible(false);
        });
        btReset.addActionListener(actionEvent -> {
            try {
                DAL.getInstance().resetKeybindings();
                initKeybindings();
                paKeybindings.updateUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Adding
        paBottom.add(btCancel);
        paBottom.add(btReset);
        paBottom.add(btSave);

        paCenter.add(paReference);
        paCenter.add(paKeybindings);

        this.add(paBottom, BorderLayout.SOUTH);
        this.add(paCenter, BorderLayout.CENTER);
    }

    private void initKeybindings() throws IOException {

        // first of all clear the panels
        paReference.removeAll();
        paKeybindings.removeAll();

        String[] keyboard = new String[]
                {
                        "1", "2", "3", "C",
                        "4", "5", "6", "D",
                        "7", "8", "9", "E",
                        "A", "0", "B", "F"
                };

        // init reference keyboard
        for (String key : keyboard) {
            JButton btn = new JButton(key);
            btn.setEnabled(false);
            paReference.add(btn);
        }

        keybindings = DAL.getInstance().loadKeybindings();

        for (String key : keyboard) {
            JButton btn = new JButton(keybindings.get(key.toUpperCase()));
            btn.setEnabled(true);

            btn.addActionListener(actionEvent -> {
                KeyDialog diaKey = new KeyDialog();
                diaKey.openKeyDialog();
                if (diaKey.isToBeSaved()) {
                    String newKey = diaKey.getKey();

                    btn.setText(newKey);
                    keybindings.put(key.toLowerCase(), newKey.toLowerCase());
                }
            });

            paKeybindings.add(btn);
        }

    }

    public void openKeybindingsDialog() {
        try {
            initKeybindings();
            this.isToBeSaved = false;
            this.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to load keybindings from file!", "File error", JOptionPane.ERROR_MESSAGE);
            this.setVisible(false);
            this.isToBeSaved = false;
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getKeybindings() {
        return keybindings;
    }

    public boolean isToBeSaved() {
        return isToBeSaved;
    }
}

class KeyDialog extends JDialog {

    private JPanel paBottom, paCenter;
    private JButton btCancel, btSave;
    private JLabel lbInfo;
    private JLabel lbOutput;

    private String key;
    private boolean isToBeSaved;

    public KeyDialog() {
        initComponents();
    }

    private void initComponents() {
        this.setSize(100, 100);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);
        this.setModal(true);

        // other components
        paBottom = new JPanel(new GridLayout(0, 2));
        paCenter = new JPanel(new BorderLayout());

        lbInfo = new JLabel("Please press any button");
        lbOutput = new JLabel("<press any button>");

        btCancel = new JButton("Cancel");
        btSave = new JButton("Save");

        // settings
        lbOutput.setHorizontalAlignment(JLabel.CENTER);

        // listeners
        btSave.addActionListener(actionEvent -> {
            this.isToBeSaved = true;
            this.setVisible(false);
        });
        btCancel.addActionListener(actionEvent -> {
            this.isToBeSaved = false;
            this.setVisible(false);
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            lbOutput.setText(e.getKeyChar() + "");
            key = e.getKeyChar() + "";
            return false;
        });

        // Adding
        paBottom.add(btCancel);
        paBottom.add(btSave);

        paCenter.add(lbInfo, BorderLayout.NORTH);
        paCenter.add(lbOutput, BorderLayout.CENTER);

        this.add(paBottom, BorderLayout.SOUTH);
        this.add(paCenter, BorderLayout.CENTER);
    }


    public void openKeyDialog() {
        this.isToBeSaved = false;
        this.setVisible(true);
    }

    public String getKey() {
        return key;
    }

    public boolean isToBeSaved() {
        return isToBeSaved;
    }
}