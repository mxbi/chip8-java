package com.mxbi.chip8;

import java.util.Arrays;

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
	private DisplayInterface display;
	private KeyboardInterface keyboard;

	// Timers
	private DelayTimer delay = new DelayTimer();
	private SoundTimer sound = new SoundTimer();

	CPU(short[] program, DisplayInterface display, KeyboardInterface keyboard) {
		this.display = display;
		this.keyboard = keyboard;

		int progload = 0x200; // Pointer to load the program at
		for (short b : program) {
			ram[progload] = b;
			progload++;
		}

		short[] charmap = {
				0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
				0x20, 0x60, 0x20, 0x20, 0x70, // 1
				0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
				0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
				0x90, 0x90, 0xF0, 0x10, 0x10, // 4
				0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
				0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
				0xF0, 0x10, 0x20, 0x40, 0x40, // 7
				0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
				0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
				0xF0, 0x90, 0xF0, 0x90, 0x90, // A
				0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
				0xF0, 0x80, 0x80, 0x80, 0xF0, // C
				0xE0, 0x90, 0x90, 0x90, 0xE0, // D
				0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
				0xF0, 0x80, 0xF0, 0x80, 0x80  // F
		};

		for (int i=0; i<charmap.length; i++) {
			ram[i] = charmap[i];
		}
	}

	public void run() throws InterruptedException {
		long cycleWaitNanos = (long) (1e9 / cpu_freq);

		long t0 = System.nanoTime();
		while (true) {
			delay.check();
			sound.check();
			display.check();

			execute();

			// Timing: Wait for next clock cycle
			long t1 = System.nanoTime();

			long waitTime = cycleWaitNanos - (t1 - t0);

			// Sleep until we have 500 microseconds left, then switch to busywait for better precision
			// Windows has crappy timers so we need this hack
			int sleepTime = (int) (waitTime - 500000);
			if (sleepTime > 0) {
				Thread.sleep(sleepTime / 1000000, sleepTime % 1000000);
			}

			// Busy wait until we reach next clock cycle
			while ((t1 - t0) < cycleWaitNanos) {
				t1 = System.nanoTime();
			}

			t0 = System.nanoTime();
		}
	}


	public static String instrToString(int instr) {
		return String.format("0x%04X", instr);
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
//		System.out.println(instrToString(instr));

		int oldPC = PC;

		switch ((instr & 0xF000) >>> 12) {
			case 0x0: switch (instr) {
				case 0x00E0: op_00E0(instr); break;
				case 0x00EE: op_00EE(instr); break;
				default:
					System.out.println("0x0nnn instruction intentionally ignored: " + instrToString(instr));
					next();
					break;
			}; break;
			case 0x1: op_1nnn(instr); break;
			case 0x2: op_2nnn(instr); break;
			case 0x3: op_3xkk(instr); break;
			case 0x4: op_4xkk(instr); break;
			case 0x5: op_5xy0(instr); break;
			case 0x6: op_6xkk(instr); break;
			case 0x7: op_7xkk(instr); break;
			case 0x8: switch (instr & 0x000F) {
				case 0x0: op_8xy0(instr); break;
				case 0x1: op_8xy1(instr); break;
				case 0x2: op_8xy2(instr); break;
				case 0x3: op_8xy3(instr); break;
				case 0x4: op_8xy4(instr); break;
				case 0x5: op_8xy5(instr); break;
				case 0x6: op_8xy6(instr); break;
				case 0x7: op_8xy7(instr); break;
				case 0xE: op_8xyE(instr); break;
				default: throw new UnsupportedOperationException("Unexpected arithmetic instruction " + instrToString(instr));
			}; break;
			case 0x9: op_9xy0(instr); break;
			case 0xA: op_Annn(instr); break;
			case 0xB: op_Bnnn(instr); break;
			case 0xC: op_Cxkk(instr); break;
			case 0xD: op_Dxyn(instr); break;
			case 0xE: switch (getk(instr)) {
				case 0x9E: op_Ex9E(instr); break;
				case 0xA1: op_ExA1(instr); break;
				case 0x7F:
				default: throw new UnsupportedOperationException("Unexpected skip instruction " + instrToString(instr));
			}; break;
			case 0xF: switch (getk(instr)) {
				case 0x07: op_Fx07(instr); break;
				case 0x0A: op_Fx0A(instr); break;
				case 0x15: op_Fx15(instr); break;
				case 0x18: op_Fx18(instr); break;
				case 0x1E: op_Fx1E(instr); break;
				case 0x29: op_Fx29(instr); break;
				case 0x33: op_Fx33(instr); break;
				case 0x55: op_Fx55(instr); break;
				case 0x65: op_Fx65(instr); break;
				default: throw new UnsupportedOperationException("Unexpected instruction " + instrToString(instr));
			}; break;
			default: throw new UnsupportedOperationException("Unexpected instruction " + instrToString(instr));
		}

		if (oldPC == PC) {
			System.out.println("Issue with " + instrToString(instr) + ": PC stuck");
		}
	}

	// INSTRUCTIONS

	// Clear the display
	private void op_00E0(int instr) {
		display.clear();
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
		PC = (short) getn(instr);
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
		V[getx(instr)] %= 0x100;
		next();
	}

	// V[x] <- V[y]
	private void op_8xy0(int instr) {
		V[getx(instr)] = V[gety(instr)];
		next();
	}

	// V[x] <- V[x] | V[y]
	private void op_8xy1(int instr) {
		V[getx(instr)] |= V[gety(instr)];
		next();
	}

	// V[x] <- V[x] & V[y]
	private void op_8xy2(int instr) {
		V[getx(instr)] &= V[gety(instr)];
		next();
	}

	// V[x] <- V[x] ^ V[y]
	private void op_8xy3(int instr) {
		V[getx(instr)] ^= V[gety(instr)];
		next();
	}

	// V[x] <- V[x] + V[y], set V[F] as carry
	private void op_8xy4(int instr) {
		int sum = V[getx(instr)] + V[gety(instr)];
		V[0xF] = (short) ((sum > 0xFF) ? 1 : 0);
		V[getx(instr)] = (short)(sum % 0x100);
		next();
	}

	// V[x] <- V[x] - V[y], set V[F] = NOT borrow
	private void op_8xy5(int instr) {
		int x = getx(instr);
		int y = gety(instr);
		if (V[y] > V[x]) { // borrow!
			V[0xF] = 0;
			V[x] += 0x100 - V[y];
		} else {
			V[0xF] = 1;
			V[x] -= V[y];
		}

		next();

		// Sanity checks
		assert V[x] < 0x100;
		assert V[x] >= 0x0;
	}

	// V[x] >>= 1, V[F] = LSB
	private void op_8xy6(int instr) {
		int x = getx(instr);
		V[0xF] = (short) (V[x] & 0x1);
		V[x] >>>= 1;

		next();
	}

	// V[x] <- V[y] - V[x], set V[F] = NOT borrow
	// very similar to 0x8xy5
	private void op_8xy7(int instr) {
		int x = getx(instr);
		int y = gety(instr);
		if (V[x] > V[y]) { // borrow
			V[0xF] = 0;
			V[x] = (short) (0x100 + V[y] - V[x]);
		} else {
			V[0xF] = 1;
			V[x] = (short) (V[y] - V[x]);
		}

		next();

		// Sanity checks
		assert V[x] < 0x100;
		assert V[x] >= 0x0;
	}

	// V[x] <<=1, V[F] = MSB
	private void op_8xyE(int instr) {
		int x = getx(instr);
		V[0xF] = (short) ((V[x] >> 7)); //((V[x] & 0b10000000));
		V[x] = (short) ((V[x] << 1) & 0xFF);

		next();
	}

	// Skip if V[x] != V[y]
	private void op_9xy0(int instr) {
		if (V[getx(instr)] != V[gety(instr)]) {
			next();
		}
		next();
	}

	// I <- NNN
	private void op_Annn(int instr) {
		I = (short) getn(instr);
		next();
	}

	// Jump to NNN+V[0]
	private void op_Bnnn(int instr) {
		PC = (short) (getn(instr) + V[0]);
	}

	// V[x] <- NN & rand()
	private void op_Cxkk(int instr) {
		int rand = (int) (Math.random() * 256);
		V[getx(instr)] = (short) (getk(instr) & rand);
		next();
	}

	// Draw sprite of width 8 and height N at (V[x], V[y]) (N bytes)
	private void op_Dxyn(int instr) {
		int N = instr & 0x000F;
		// We'll worry about it later
		V[0xF] = (short) (display.draw(Arrays.copyOfRange(ram, I, I+N), V[getx(instr)], V[gety(instr)]) ? 1 : 0);
		next();
	}

	// Skip next instruction if key stored in V[x] is pressed
	private void op_Ex9E(int instr) {
		if (keyboard.isPressed(getx(instr))) {
			next();
		}
		next();
	}

	// Skip next instruction if key stored in V[x] is NOT pressed
	private void op_ExA1(int instr) {
		if (!keyboard.isPressed(gety(instr))) {
			next();
		}
		next();
	}

	// Set V[x] to the time left on the delay timer
 	private void op_Fx07(int instr) {
		V[getx(instr)] = (short) delay.getTimer();
		next();
	}

	// Wait until key is pressed, and put that key in V[x]
	private void op_Fx0A(int instr) {
		int key = keyboard.waitForAnyKey();
		if (key > 0xFF) {
			throw new IllegalStateException("0xFx0A: Key" + key + "pressed out of range");
		}
		V[getx(instr)] = (short) key;

		next();
	}

	// Set delay timer to V[x]
	private void op_Fx15(int instr) {
		delay.setTimer(V[getx(instr)]);
		next();
	}

	// Set sound timer to V[x]
	private void op_Fx18(int instr) {
		sound.setTimer(V[getx(instr)]);
		next();
	}

	// I += V[x]. V[F] = 1 if result overflows 0xFFF
	private void op_Fx1E(int instr) {
		 I += V[getx(instr)];
		 if (I > 0xFFF) {
		 	I %= 0x1000;
		 	V[0xF] = 1;
		 } else {
		 	V[0xF] = 0;
		 }

		 next();
	}

	// Set I to location of character in V[x]
	private void op_Fx29(int instr) {
		I = (short) (5*V[getx(instr)]);
		next();
	}

	// Store BCD representation of V[x] at I, I+1, I+2
	private void op_Fx33(int instr) {
		int num = V[getx(instr)];
		int hundreds = num / 100;
		int tens = (num / 10) % 10;
		int ones = num % 10;
		ram[I  ] = (short) hundreds;
		ram[I+1] = (short) tens;
		ram[I+2] = (short) ones;

		next();
	}

	// Dump contents of V[:x] starting at ram[I]
	private void op_Fx55(int instr) {
		for (int i=0; i <= getx(instr); i++) {
			ram[I+i] = V[i];
		}
		next();
	}

	// Load contents into V[:x] starting at ram[I]
	private void op_Fx65(int instr) {
		for (int i=0; i <= getx(instr); i++) {
			V[i] = ram[I+i];
		}
		next();
	}
}
