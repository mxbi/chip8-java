package com.mxbi.chip8;

public interface KeyboardInterface {
    boolean isPressed(int key);
    int waitForAnyKey();
}
