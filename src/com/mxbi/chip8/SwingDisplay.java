package com.mxbi.chip8;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

public class SwingDisplay implements DisplayInterface, KeyboardInterface {
    private boolean[][] disp = new boolean[64][32];

    private JFrame frame;
    private JLabel label;
    private ImageIcon imageIcon;

    SlidingBuffer<Long> frameTimer = new SlidingBuffer<>(60);
    SlidingBuffer<Long> execTimer = new SlidingBuffer<>(512);

    public static final long minFrameTimeNanos = (long) 1.667e7; // 60 fps

    private void createGUI() {
        frame = new JFrame("CHIP-8");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(670, 380));

        frame.getContentPane().setBackground(Color.DARK_GRAY);

        imageIcon = new ImageIcon();
        label = new JLabel(imageIcon);

        drawNewFrame();

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

    private void drawNewFrame() {
        // Update the frame itself
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

        // Update the FPS/Clock counters
        frameTimer.push(System.nanoTime());

        if (frameTimer.isFilled() && execTimer.isFilled()) {

            double nanosPerFrame = (double) (frameTimer.getLast() - frameTimer.getNthLast()) / frameTimer.getN();
            double nanosPerClock = (double) (execTimer.getLast() - execTimer.getNthLast()) / execTimer.getN();

            double fps = 1e9 / nanosPerFrame;
            double freq = 1e9 / nanosPerClock;

            frame.setTitle(String.format("CHIP-8: %.1f Hz %.1f FPS", freq, fps));
        }
    }

    @Override
    // Called on every clock cycle
    public void check() {
        execTimer.push(System.nanoTime());
        if (System.nanoTime() - frameTimer.getLast() > minFrameTimeNanos) {
            drawNewFrame();
        }
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
