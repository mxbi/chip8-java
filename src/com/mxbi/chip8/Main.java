package com.mxbi.chip8;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
	   short[] rom = ROMLoader.loadRomFromFile("BC_test.ch8");

	   Display display = new Display();
	   CPU cpu = new CPU(rom, display, display);
	   cpu.run();
    }
}
