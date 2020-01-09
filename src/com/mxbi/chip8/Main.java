package com.mxbi.chip8;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
	   short[] rom = ROMLoader.loadRomFromFile("keypad-test.ch8");

	   SwingDisplay display = new SwingDisplay();
	   CPU cpu = new CPU(rom, display, display);
	   cpu.run();
    }
}
