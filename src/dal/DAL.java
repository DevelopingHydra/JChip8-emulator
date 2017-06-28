package dal;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/**
 * Created by xeniu on 21.05.2017.
 */
public class DAL {
    private static DAL instance;


    private DAL() {
    }

    public static DAL getInstance() {
        if (DAL.instance == null) {
            DAL.instance = new DAL();
        }
        return instance;
    }

    private final String basePath = System.getProperty("user.dir") + File.separator + "src" + File.separator;

    private final File configFile_keybindings = new File(basePath + "config" + File.separator + "keybinding.conf");
    private final File configFile_settings = new File(basePath + "config" + File.separator + "settings.conf");

    public String[] listAvailableSoundFiles() {
        File folder = new File(this.basePath + "sounds" + File.separator);
        File[] listOfFiles = folder.listFiles();
        LinkedList<String> filenames = new LinkedList<>();

        filenames.add("system-default");

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                filenames.add(listOfFiles[i].getName());
            }
        }

        return filenames.toArray(new String[filenames.size()]);
    }

    public void saveSettings(HashMap<String, String> hmSettings) throws IOException {
        saveHashmapToFile(configFile_settings, hmSettings);
    }

    public HashMap<String, String> loadSettings() throws IOException {
        return loadFileToHashmap(configFile_settings);
    }

    public void resetSettings() throws IOException {
        HashMap<String, String> hmSettings = new HashMap<>();

        hmSettings.put("color_background", "FFFFFF");
        hmSettings.put("color_foreground", "000000");
        hmSettings.put("speed", "60");
        hmSettings.put("mode_eyesore", "false");

        saveHashmapToFile(configFile_settings, hmSettings);
    }

    public HashMap<String, String> loadKeybindings() throws IOException {
        return loadFileToHashmap(configFile_keybindings);
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
        HashMap<String, String> hmKeybindings = new HashMap<>();

        hmKeybindings.put("1", "1");
        hmKeybindings.put("2", "2");
        hmKeybindings.put("3", "3");
        hmKeybindings.put("q", "4");
        hmKeybindings.put("w", "5");
        hmKeybindings.put("e", "6");
        hmKeybindings.put("a", "7");
        hmKeybindings.put("s", "8");
        hmKeybindings.put("d", "9");
        hmKeybindings.put("x", "0");
        hmKeybindings.put("y", "A");
        hmKeybindings.put("c", "B");
        hmKeybindings.put("4", "C");
        hmKeybindings.put("r", "D");
        hmKeybindings.put("f", "E");
        hmKeybindings.put("v", "F");

        saveHashmapToFile(configFile_keybindings, hmKeybindings);
    }

    private HashMap<String, String> loadFileToHashmap(File file) throws IOException {
        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream(file);
        defaultProps.load(in);
        in.close();

        HashMap<String, String> hashmap = new HashMap<>();

        for (String key : defaultProps.stringPropertyNames()) {
            String value = defaultProps.getProperty(key);
            hashmap.put(key, value);
        }
        return hashmap;
    }

    private void saveHashmapToFile(File file, HashMap<String, String> hashmap) throws IOException {
        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream(file);
        defaultProps.load(in);
        in.close();

        for (Map.Entry<String, String> entry : hashmap.entrySet()) {
            defaultProps.setProperty(entry.getKey(), entry.getValue());
        }

        FileOutputStream out = new FileOutputStream(file);
        defaultProps.store(out, "by Manuel Kollmann");
        out.close();
    }
}
