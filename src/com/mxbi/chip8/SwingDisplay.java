package com.mxbi.chip8;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

public class SwingDisplay implements DisplayInterface, KeyboardInterface {
    boolean[][] disp = new boolean[64][32];
    JFrame frame;
    JLabel label;
    Graphics gfx;

    ImageIcon imageIcon;

    private void createGUI() {
        frame = new JFrame("CHIP-8");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(670, 380));

        frame.getContentPane().setBackground(Color.DARK_GRAY);

        imageIcon = new ImageIcon();
        label = new JLabel(imageIcon);

        output();

//        label = new JLabel("Hello World");
        frame.getContentPane().add(label);
        frame.pack();
        frame.setVisible(true);
//
//        gfx = frame.getGraphics();
//        gfx.fillRect(10, 10, 100, 100);
    }

    public SwingDisplay() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(this::createGUI);
    }

    public void clear() {
        disp = new boolean[64][32];
    }

    private boolean getBit(short num, int bit) {
        return ((num >> bit) & 1) == 1;
    }

    public boolean draw(short[] data, int x0, int y0) {
        boolean flipped = false;

        for (int y=0; y<data.length; y++) {
            for (int x = 0; x < 8; x++) {
                boolean bit = getBit(data[y], 7 - x);
                int xco = (x0 + x) % 64;
                int yco = (y0 + y) % 32;
                if (bit) {
                    if (disp[xco][yco]) {
                        disp[xco][yco] = false;
                        flipped = true;
                    } else {
                        disp[xco][yco] = true;
                    }
                }
            }
        }
        return flipped;
    }

    private void output() {
        BufferedImage bi = new BufferedImage(640, 320, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D gfx = bi.createGraphics();
        gfx.setColor(Color.WHITE);

        for (int y=0; y<32; y++) {
            for (int x=0; x<64; x++) {
                if (disp[x][y]) {
                    gfx.fillRect(x * 10, y * 10, 10, 10);
                } else {
                    gfx.clearRect(x * 10, y * 10, 10, 10);
                }
            }
        }

        imageIcon.setImage(bi);
        frame.repaint();

    }

    @Override
    // Called on every clock cycle
    public void check() {
        // Draw on every cycle for now
        output();
    }

    @Override
    public boolean isPressed(int key) {
        System.out.println("KEY ispressed? checked " + key);
        return false;
    }

    @Override
    public int waitForAnyKey() {
        System.out.println("waited for any key!");
        return 0;
    }
}
