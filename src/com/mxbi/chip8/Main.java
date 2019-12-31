package com.mxbi.chip8;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	   short[] rom = ROMLoader.loadRomFromFile("test_opcode.ch8");
	   System.out.println(CPU.instrToString(rom[0]));

	   CPU cpu = new CPU(rom);
	   cpu.execute();
	   cpu.execute();
	   cpu.execute();
    }
}
