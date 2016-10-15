package com.braga.utils;

public class StopWatch {
    private boolean running;
    private long startTime;
    private long stopTime;

    public StopWatch() {
        this.startTime = 0;
        this.stopTime = 0;
        this.running = false;
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }

    public long getElapsedTime() {
        if (this.running) {
            return System.currentTimeMillis() - this.startTime;
        }
        return this.stopTime - this.startTime;
    }

    public long getElapsedTimeSecs() {
        if (this.running) {
            return (System.currentTimeMillis() - this.startTime) / 1000;
        }
        return (this.stopTime - this.startTime) / 1000;
    }
}
