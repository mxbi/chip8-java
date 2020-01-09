package com.mxbi.chip8;

// Efficient buffer that keeps the last N elements.
// This is used for keeping track of precision timers over a moving average (eg. framerate averaged over the last 60 frametimes)
public class SlidingBuffer<T> {
	private T[] buf;
	private int ptr = 0;
	private int size;
	private boolean filled = false;

	SlidingBuffer(int size) {
		this.size = size;
		buf = (T[]) new Object[size];
	}

	public void push(T value) {
		buf[ptr] = value;
		ptr += 1;
		if (ptr >= size) {
			ptr = 0;
			filled = true;
		}
	}

	public boolean isFilled() {
		return filled;
	}

	// Nth-last element to be pushed into the buffer
	public T getNthLast() {
		return buf[ptr];
	}

	// Last element to be pushed into the buffer
	public T getLast() {
		if (ptr == 0) { // Edge case
			return buf[size-1];
		}
		return buf[ptr-1];
	}

	public int getN() {
		return size;
	}
}
