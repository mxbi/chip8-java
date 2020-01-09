package com.mxbi.chip8;
import javax.sound.sampled.*;

// Credit: Most of this class comes from https://stackoverflow.com/a/6700039/5128131
public class SoundUtils {

	public static float SAMPLE_RATE = 24000f;

	public static void tone(int hz, int msecs)
			throws LineUnavailableException
	{
		tone(hz, msecs, 1.0);
	}

	public static void tone(int hz, int msecs, double vol)
			throws LineUnavailableException
	{
		byte[] buf = new byte[1];
		AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
		SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
		sdl.open(af);
		sdl.start();
		for (int i=0; i < msecs*8; i++) {
			double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
			buf[0] = (byte)(Math.sin(angle) * 127.0 * vol);
			sdl.write(buf,0,1);
		}
		sdl.drain();
		sdl.stop();
		sdl.close();
	}

	public static void playSine(int hz, int msecs) {
		new Thread(() -> {
			try {
				SoundUtils.tone(hz , msecs);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public static void main(String[] args) throws Exception {
		playSine(1000, 1000);
		System.out.println("unblocked!");
	}
}