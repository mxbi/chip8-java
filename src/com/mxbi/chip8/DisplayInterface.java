package com.mxbi.chip8;

public interface DisplayInterface {
    void clear();

    // Draw should return true if any bits are _unset_ by the sprite, and false otherwise
    boolean draw(short[] data, int x, int y);

    void check();
}
