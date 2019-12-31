package com.mxbi.chip8;

public class CPU {
	public static final int cpu_freq = 512;

	// Initialise memory and registers
	// We unfortunately have to wasteful and use short instead of byte here to allow for up to 0xFF representation
	// Since Java has no unsigned primitives
	// Throughout this project, we use short for storing 8-bit bytes, and int for storing 16-bit shorts *sigh*
	private short[] ram = new short[4096]; // Main memory
	private short PC = 0x200; // Program counter

	private short[] V = new short[16]; // 8-bit GP registers

	private short I; // 16 bit register

	// Execution stack
	private short[] stack = new short[16];
	private byte SP = 0x0;

	// 32x64 monochrome display
	private boolean[][] display = new boolean[32][64];

	// Timers
	private DelayTimer delay = new DelayTimer();
	private SoundTimer sound = new SoundTimer();

	CPU(short[] program) {
		int progload = 0x200; // Pointer to load the program at
		for (short b : program) {
			ram[progload] = b;
			progload++;
		}
	}

	public static String instrToString(int instr) {
		return Integer.toHexString(instr & 0xFFFF);
	}

	public static int getn(int instr) {
		return instr & 0x0FFF;
	}

	public static int getk(int instr) {
		return instr & 0x00FF;
	}

	public static int getx(int instr) {
		return (instr & 0x0F00) >> 8;
	}

	public static int gety(int instr) {
		return (instr & 0x00F0) >> 4;
	}

	// Increment program counter
	private void next() {
		PC += 0x2;
	}

	void execute() throws UnsupportedOperationException {
		// Fetch instruction (2 bytes) from ram
		int instr = (((int) ram[PC]) << 8) + ((int) ram[PC + 1]);

		switch ((instr & 0xF000) >>> 12) {
			case 0x0: switch (instr) {
				case 0x00E0: op_00E0(instr); break;
				case 0x00EE: op_00EE(instr); break;
				default: throw new UnsupportedOperationException("0x0nnn instruction intentionally not implemented" + instrToString(instr));
			}

			case 0x1: op_1nnn(instr); break;
			case 0x2: op_2nnn(instr); break;
			case 0x3: op_3xkk(instr); break;
			case 0x4: op_4xkk(instr); break;
			case 0x5: op_5xy0(instr); break;
			case 0x6: op_6xkk(instr); break;
			case 0x7: op_7xkk(instr); break;
			case 0x8: switch (instr & 0x000F) {
				case 0x0: op_8xy0(instr); break;
//				case 0x1: op_8xy1(instr); break;
//				case 0x2: op_8xy2(instr); break;
//				case 0x3: op_8xy3(instr); break;
//				case 0x4: op_8xy4(instr); break;
//				case 0x5: op_8xy5(instr); break;
//				case 0x6: op_8xy6(instr); break;
//				case 0x7: op_8xy7(instr); break;
//				case 0xE: op_8xyE(instr); break;
				default: throw new UnsupportedOperationException("Unexpected arithmetic instruction " + instrToString(instr));
			}
//			case 0x9: op_9xy0(instr); break;
//			case 0xA: op_Annn(instr); break;
//			case 0xB: op_Bnnn(instr); break;
//			case 0xC: op_Cxkk(instr); break;
//			case 0xD: op_Dxyn(instr); break;
//			case 0xE: switch (getk(instr)) {
//				case 0x9E: op_Ex9E(instr); break;
//				case 0xA1: op_ExA1(instr); break;
//				case 0x7F:
//				default: throw new UnsupportedOperationException("Unexpected skip instruction " + instrToString(instr));
//			}
//			case 0xF: switch (getk(instr)) {
//				case 0x65: op_Ex9E(instr); break;
//				case 0x07: op_Fx07(instr); break;
//				case 0x0A: op_Fx0A(instr); break;
//				case 0x15: op_Fx15(instr); break;
//				case 0x18: op_Fx18(instr); break;
//				case 0x1E: op_Fx1E(instr); break;
//				case 0x29: op_Fx29(instr); break;
//				case 0x33: op_Fx33(instr); break;
//				case 0x55: op_Fx55(instr); break;
//				case 0x65: op_Fx65(instr); break;
//				default: throw new UnsupportedOperationException("Unexpected instruction " + instrToString(instr));
//			}
			default: throw new UnsupportedOperationException("Unexpected instruction " + instrToString(instr));
		}
	}

	// INSTRUCTIONS

	// Clear the display
	private void op_00E0(int instr) {
		display = new boolean[32][64];
		next();
	}

	// Return from subroutine (pop stack)
	private void op_00EE(int instr) {
		if (SP < 0) {
			throw new IllegalStateException("0x00EE: Tried to pop empty stack");
		}
		PC = stack[SP];
		SP -= 1;
		next();
	}

	// Unconditional jump
	private void op_1nnn(int instr) {
		PC = (short)getn(instr);
	}

	// Call subroutine
	private void op_2nnn(int instr) {
		SP += 1;
		stack[SP] = PC;
		PC = (short) getn(instr);
	}

	// Skip instruction if V[x] == kk
	private void op_3xkk(int instr) {
		if (V[getx(instr)] == getk(instr)) {
			next();
		}
		next();
	}

	// Skip instruction if V[x] != kk
	private void op_4xkk(int instr) {
		if (V[getx(instr)] != getk(instr)) {
			next();
		}
		next();
	}

	// Skip if V[x] == V[y]
	private void op_5xy0(int instr) {
		if (V[getx(instr)] == V[gety(instr)]) {
			next();
		}
		next();
	}

	// V[x] <- kk
	private void op_6xkk(int instr) {
		V[getx(instr)] = (short)getk(instr);
		next();
	}

	// V[x] += kk
	private void op_7xkk(int instr) {
		V[getx(instr)] += getk(instr);
		next();
	}

	// V[x] <- V[y]
	private void op_8xy0(int instr) {
		V[getx(instr)] = V[gety(instr)];
		next();
	}
}
