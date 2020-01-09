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

    public static final int frameRate = 60;
    private static final long minFrameTimeNanos = (long) (1e9 / frameRate); // 60 fps
    private long nextFrameTime = System.nanoTime();

    // 1 second average for FPS/clock speed timers
    private SlidingBuffer<Long> frameTimer = new SlidingBuffer<>(frameRate);
    private SlidingBuffer<Long> execTimer = new SlidingBuffer<>(CPU.cpu_freq);

    private void createGUI() {
        frame = new JFrame("CHIP-8");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(670, 380));

        frame.getContentPane().setBackground(Color.DARK_GRAY);

        imageIcon = new ImageIcon();
        label = new JLabel(imageIcon);

        drawNewFrame();
        
        frame.getContentPane().add(label);
        frame.pack();
        frame.setVisible(true);

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

            double nanosPerFrame = (double) (frameTimer.getLast() - frameTimer.getNthLast()) / (frameTimer.getN() - 1);
            double nanosPerClock = (double) (execTimer.getLast() - execTimer.getNthLast()) / (execTimer.getN() - 1);

            double fps = 1e9 / nanosPerFrame;
            double freq = 1e9 / nanosPerClock;

            frame.setTitle(String.format("CHIP-8: %.1f Hz %.1f FPS", freq, fps));
        }

        // Queue the next frame
        nextFrameTime += minFrameTimeNanos;
    }

    @Override
    // Called on every clock cycle
    public void check() {
        long nanoTime = System.nanoTime();
        execTimer.push(nanoTime);

        // We increment it by 16ms instead of just adding 16ms onto the current time
        // This means that each frame compensates for the delay of the last frame, giving more accurate timing
//        if (nextFrameTime == null) {
//            nextFrameTime = nanoTime - 1;
//        }
        if (nanoTime > nextFrameTime) {
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
