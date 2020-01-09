package com.mxbi.chip8;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;

public class SwingKeyboardListener extends KeyAdapter {
    private HashSet<Character> keysPressed = new HashSet<>();

    @Override
    public void keyPressed(KeyEvent event) {
        char ch = event.getKeyChar();
        keysPressed.add(ch);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        char ch = event.getKeyChar();
        keysPressed.remove(ch);
    }

    public boolean isPressed(char key) {
        return keysPressed.contains(key);
    }
}
