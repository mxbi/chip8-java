package com.mxbi.chip8;

import jdk.jshell.spi.ExecutionControl.NotImplementedException;

public class CPU {
	public static final int cpu_freq = 512;
	byte delay, sound, SP;
	short I, PC;
	byte[] ram, V;
	short[] stack;
	boolean[] display;

	CPU(byte[] program) {
		// Initialise memory and registers
		byte[] ram = new byte[4096];

		int progload = 0x200;
		for (byte b : program) {
			ram[progload] = b;
			progload += 1;
		}

		byte[] V = new byte[16];
		short I = 0x0;

		boolean[][] display = new boolean[32][64];

		// Timers
		byte delay = 0x0;
		byte sound = 0x0;

		// Stack/PC
		short PC = 0x200;
		short stack[] = new short[16];
		byte SP = -1;
	}

	private String instrToString(short instr) {
		return Integer.toHexString(instr & 0xFFFF);
	}

	private short getn(short instr) {
		return (short)(instr & (short)(0x0FFF));
	}

	private byte getk(short instr) {
		return (byte)(instr & (short)(0x00FF));
	}

	private byte getx(short instr) {
		return (byte)(instr & (short)(0x0F00) >> 8);
	}

	private byte gety(short instr) {
		return (byte)(instr & (short)(0x00F0) >> 4);
	}

	// Increment program counter
	private void next() {
		PC += 0x2;
	}

	void execute() throws NotImplementedException {
		// Fetch instruction (2 bytes) from ram
		short instr = (short)(ram[PC] + ram[PC + 1]);
		switch (instr & (short)0xF000 >>> 12) {
			case 0x0: switch (instr) {
				case 0x00E0: op_00E0(instr); break;
				case 0x00EE: op_00EE(instr); break;
				default: throw new NotImplementedException("0x0nnn instruction not implemented intentionally " + instrToString(instr));
			}

			case 0x1: op_1nnn(instr); break;
			case 0x2: op_2nnn(instr); break;
			case 0x3: op_3xkk(instr); break;
			case 0x4: op_4xkk(instr); break;
			case 0x5: op_5xy0(instr); break;
			case 0x6: op_6xkk(instr); break;
			case 0x7: op_7xkk(instr); break;
			case 0x8: switch (instr & (short)0x000F) {
				case 0x0: op_8xy0(instr); break;
				case 0x1: op_8xy1(instr); break;
				case 0x2: op_8xy2(instr); break;
				case 0x3: op_8xy3(instr); break;
				case 0x4: op_8xy4(instr); break;
				case 0x5: op_8xy5(instr); break;
				case 0x6: op_8xy6(instr); break;
				case 0x7: op_8xy7(instr); break;
				case 0xE: op_8xyE(instr); break;
				default: throw new NotImplementedException("Unexpected arithmetic instruction " + instrToString(instr));
			}
			case 0x9: op_9xy0(instr); break;
			case 0xA: op_Annn(instr); break;
			case 0xB: op_Bnnn(instr); break;
			case 0xC: op_Cxkk(instr); break;
			case 0xD: op_Dxyn(instr); break;
			case 0xE: switch (getk(instr)) {
				case 0x9E: op_Ex9E(instr); break;
				case 0xA1: op_ExA1(instr); break;
				case 0x7F:
				default: throw new NotImplementedException("Unexpected skip instruction " + instrToString(instr));
			}
			case 0xF: switch (getk(instr)) {
				case 0x65: op_Ex9E(instr); break;
				case 0x07: op_Fx07(instr); break;
				case 0x0A: op_Fx0A(instr); break;
				case 0x15: op_Fx15(instr); break;
				case 0x18: op_Fx18(instr); break;
				case 0x1E: op_Fx1E(instr); break;
				case 0x29: op_Fx29(instr); break;
				case 0x33: op_Fx33(instr); break;
				case 0x55: op_Fx55(instr); break;
				case 0x65: op_Fx65(instr); break;
				default: throw new NotImplementedException("Unexpected instruction " + instrToString(instr));
			}
		}
	}

	// Clear the display
	private void op_00E0(short instr) {
		boolean[][] display = new boolean[32][64];
		next();
	}

	// Return from subroutine (pop stack)
	private void op_00EE(short instr) {
		if (SP < 0) {
			throw new IllegalStateException("0x00EE: Tried to pop empty stack");
		}
		PC = stack[SP];
		SP -= 1;
	}

	// Unconditional jump
	private void op_1nnn(short instr) {
		PC = getn(instr);
	}

	// Call subroutine
	private void op_2nnn(short instr) {
		SP += 1;
		stack[SP] = PC;
		PC = getn(instr);
	}

	// Skip instruction if V[x] == kk
	private void op_3xkk(short instr) {
		if (V[getx(instr)] == getk(instr)) {
			next();
		}
		next();
	}

	// Skip instruction if V[x] != kk
	private void op_4xkk(short instr) {
		if (V[getx(instr)] != getk(instr)) {
			next();
		}
		next();
	}

	// Skip if V[x] == V[y]
	private void op_5xy0(short instr) {
		if (V[getx(instr)] == V[gety(instr)]) {
			next();
		}
		next();
	}

	// V[x] <- kk
	private void op_6xkk(short instr) {
		V[getx(instr)] = getk(instr);
		next();
	}

	// V[x] += kk
	private void op_7xkk(short instr) {
		V[getx(instr)] += getk(instr);
		next();
	}

	// V[x] <- V[y]
	private void op_8xy0(short instr) {
		V[getx(instr)] = V[gety(instr)];
		next();
	}
}
