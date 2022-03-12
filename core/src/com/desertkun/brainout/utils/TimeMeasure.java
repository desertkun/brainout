package com.desertkun.brainout.utils;

public class TimeMeasure
{
    private long time;
    private float took;

    public void start()
    {
        this.time = System.nanoTime();
    }

    public void end()
    {
        this.took = (System.nanoTime() - this.time) / 1000000000.0f;
    }

    public void end_plus()
    {
        this.took += (System.nanoTime() - this.time) / 1000000000.0f;
    }

    public long getTook()
    {
        long a = (long)(took * 1000.0f);
        took = 0;
        return a;
    }
}
