package com.mxbi.chip8;

public class SoundTimer extends Timer {
	@Override
	void onSet(int count) {
		System.out.println("BEEP!");
		// TODO: Variable length beep
//		java.awt.Toolkit.getDefaultToolkit().beep();
		SoundUtils.playSine(1000, (int) (timeBetweenUpdates / 1e6) * count);
	}
}
