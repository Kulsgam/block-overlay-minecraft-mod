package com.kulsgam.utils;

public class Animator {
    private final double duration;
    private long startTime;

    public Animator(double duration) {
        this.duration = duration;
    }

    public double getValue(double start, double end, boolean increasing, boolean quadratic) {
        double time = (System.currentTimeMillis() - startTime) / duration;
        time = quadratic ? 2.0 * time * time : (time -= 1.0) * time * time + 1.0;
        double value = increasing ? start + time * (end - start) : end + time * (start - end);
        if (increasing && end < value) {
            value = end;
        } else if (!increasing && value < start) {
            value = start;
        }
        return value;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
    }
}
