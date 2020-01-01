package com.mxbi.chip8;

public class Display implements DisplayInterface, KeyboardInterface {
    boolean[][] disp = new boolean[64][32];

    public Display() {

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
                boolean bit = getBit(data[y], x);
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
        for (int y=0; y<32; y++) {
            for (int x=0; x<64; x++) {
                System.out.print(disp[x][y] ? '#' : ' ');
            }
            System.out.println();
        }
    }

    @Override
    public void check() {
        // Draw on every cycle for now
        output();
    }

    @Override
    public boolean isPressed(int key) {
        System.out.println("KEY ispressed? checked");
        return false;
    }

    @Override
    public int waitForAnyKey() {
        System.out.println("waited for any key!");
        return 0;
    }
}
