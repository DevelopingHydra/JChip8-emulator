package main;

import gui.GUI;

import java.io.*;

/**
 * Created by xeniu on 02.04.2017.
 */
public class Main {
    public static void main(String[] args) {

//        if (true) return;

        GUI gui = new GUI();
//        testCanvas(gui);
//        testLoadGame(gui);
    }

    /**
     * todo remove in beta
     *
     * @param gui
     */
    private static void testLoadGame(GUI gui) {
        gui.onLoadGame(System.getProperty("user.dir") + File.separator + "src" + File.separator + "c8games" + File.separator + "PONG");
        gui.onStartGame();
    }

    /**
     * todo remove in beta
     *
     * @param gui
     */
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

//        gui.getCanvasPanel().setCanvas(c);
    }
}
