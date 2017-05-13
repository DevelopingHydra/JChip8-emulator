package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by xeniu on 02.04.2017.
 */
public class Canvas extends JPanel {
    private static final int WIDTH = 64, HEIGHT = 32;
    private Color bgColor, drawColor;

    private int[][] canvas;

    public Canvas() {
        canvas = new int[HEIGHT][WIDTH];
        bgColor = Color.WHITE;
        drawColor = Color.BLACK;

        this.setFocusable(true);
        this.setRequestFocusEnabled(true);
    }


    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

//        printCanvas();

        graphics.setColor(bgColor);
        graphics.fillRect(0, 0, getWidth(), getHeight());

        int _pixelWidth = getWidth() / WIDTH;
        int _pixelHeight = getHeight() / HEIGHT;

        graphics.setColor(drawColor);
        for (int i = 0; i < canvas.length; i++) {
            for (int j = 0; j < canvas[i].length; j++) {
                if (canvas[i][j] == 1) {
//                    graphics.fillRect(i * _pixelWidth, j * _pixelHeight, _pixelWidth, _pixelHeight);
                    graphics.fillRect(j * _pixelWidth, i * _pixelHeight, _pixelWidth, _pixelHeight);
                }
            }
        }
    }


    /**
     * only for testing
     */
    public void printCanvas() {
        for (int i = 0; i < 32; i++) {
            System.out.print("-");
        }
        System.out.println();
        for (int i = 0; i < canvas.length; i++) {
            for (int j = 0; j < canvas[i].length; j++) {
                System.out.print(canvas[i][j] == 1 ? "x" : " ");
            }
            System.out.println();
        }
        for (int i = 0; i < 32; i++) {
            System.out.print("-");
        }
        System.out.println();
    }


    public void resetCanvas() {
        this.canvas = new int[WIDTH][HEIGHT];
    }

    public void setCanvas(int[][] canvas) {
        this.canvas = canvas;
        repaint();
    }

    public int[][] getCanvas() {
        return canvas;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public Color getDrawColor() {
        return drawColor;
    }

    public void setDrawColor(Color drawColor) {
        this.drawColor = drawColor;
    }
}
