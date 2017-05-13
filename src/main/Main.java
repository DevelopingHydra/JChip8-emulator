package main;

import gui.GUI;

import java.io.*;
import java.util.Properties;

/**
 * Created by xeniu on 02.04.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException {

//        if (true) return;

        GUI gui = new GUI();
//        testCanvas(gui);
//        testLoadGame(gui);
    }

    private static void testLoadGame(GUI gui) {
        gui.loadGame(System.getProperty("user.dir") + File.separator + "src" + File.separator + "c8games" + File.separator + "PONG");
        gui.startGame();
    }

    private static void testCanvas(GUI gui) {
        int[][] c = new int[64][32];
        c[0][0] = 1;
        c[0][1] = 1;
        c[0][2] = 1;
        c[1][0] = 1;
        c[2][0] = 1;
        c[3][0] = 1;
        c[63][0] = 1;
        c[0][31] = 1;

        gui.getCanvasPanel().setCanvas(c);
    }
}
