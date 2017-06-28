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
    private JLabel lbSpeed, lbColorBackground, lbColorForeground, lbEyeSoreMode, lbSound;
    private JTextField tfSpeed;
    private JCheckBox cbEyeSoreMode;
    private JPanel paBottom, paGridsettingsGame;
    private JButton btCancel, btSave, btReset, btOpenColorDialogBackroung, btOpenColorDialogForeground;
    private JComboBox<String> cbSounds;

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

        tfSpeed = new JTextField();

        cbEyeSoreMode = new JCheckBox();

        cbSounds = new JComboBox<>();

        paGridsettingsGame = new JPanel(new GridLayout(0, 2));
        paBottom = new JPanel(new GridLayout(0, 3));

        btCancel = new JButton("Cancel");
        btSave = new JButton("Save");
        btReset = new JButton("Reset to default");
        btOpenColorDialogBackroung = new JButton("select color");
        btOpenColorDialogForeground = new JButton("select color");

        // listeners
        btCancel.addActionListener(actionEvent -> onCancel());
        btSave.addActionListener(actionEvent -> onSave());
        btReset.addActionListener(actionEvent -> onReset());
        btOpenColorDialogBackroung.addActionListener(actionEvent -> {
            cBackground = onOpenColorDialog();
            if (cBackground != null) btOpenColorDialogBackroung.setBackground(cBackground);
        });
        btOpenColorDialogForeground.addActionListener(actionEvent -> {
            cForeground = onOpenColorDialog();
            if (cForeground != null) btOpenColorDialogForeground.setBackground(cForeground);
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
        paGridsettingsGame.add(btOpenColorDialogBackroung);
        paGridsettingsGame.add(lbColorForeground);
        paGridsettingsGame.add(btOpenColorDialogForeground);
        paGridsettingsGame.add(lbEyeSoreMode);
        paGridsettingsGame.add(cbEyeSoreMode);

        this.add(paGridsettingsGame, BorderLayout.CENTER);
        this.add(paBottom, BorderLayout.SOUTH);
    }

    private Color onOpenColorDialog() {
        JDialog dia = new JDialog();
        dia.setSize(400, 300);
        dia.setLocationRelativeTo(null);
        dia.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dia.getContentPane().setLayout(new BorderLayout());

        JColorChooser cc = new JColorChooser();

        JPanel paBottom = new JPanel(new GridLayout(0, 2));
        JButton btCancel = new JButton("Cancel");
        JButton btSave = new JButton("Save");

        paBottom.add(btCancel);
        paBottom.add(btSave);

        dia.add(paBottom, BorderLayout.SOUTH);
        dia.getContentPane().add(cc, BorderLayout.CENTER);

        final boolean[] isToBeSaved = {false}; // I hate to do this :(

        btSave.addActionListener(actionEvent -> {
            isToBeSaved[0] = true;
            dia.setVisible(false);
        });
        btCancel.addActionListener(actionEvent -> dia.setVisible(false));

        dia.setModal(true);
        dia.setVisible(true);

        // after closed
        if (isToBeSaved[0]) {
            return cc.getColor();
        }
        return null;
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
        for(String s:sounds) {
            cbSounds.addItem(s);
        }

        try {
            HashMap<String, String> hmSettings = DAL.getInstance().loadSettings();
            for (Map.Entry<String, String> entry : hmSettings.entrySet()) {
                switch (entry.getKey()) {
                    case "color_background":
                        cBackground = Color.decode("#" + entry.getValue());
                        btOpenColorDialogBackroung.setBackground(cBackground);
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
