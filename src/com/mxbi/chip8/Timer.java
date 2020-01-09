package com.mxbi.chip8;

public abstract class Timer {
    public static int updateRate = 60;
    public static long timeBetweenUpdates = (long) (1e9 / updateRate);

    private int count;
    private long setTime = 0;
    private long nextDecrement;

    void setTimer(int count) {
        this.count = count;
        setTime = System.nanoTime();
        this.nextDecrement = setTime + timeBetweenUpdates;

        onSet(count);
    }

    int getTimer() {
        return count;
    }

    void onSet(int count) {};

    void check() {
        if (count > 0 && System.nanoTime() > nextDecrement) {
            count--;
            nextDecrement += timeBetweenUpdates;
        }
    }
}
