package com.desertkun.brainout.utils;

public class ProtectedFloat
{
    private int value;

    public ProtectedFloat(int value)
    {
        setValue(value);
    }

    public void setValue(float value)
    {
        int v = Float.floatToIntBits(value);

        int val =
            ((v & 0x000000FF) << 16) +  // 0x00FF0000
            ((v & 0x0000FF00) >>> 8) +  // 0x000000FF
            ((v & 0x00FF0000) << 8)  +  // 0xFF000000
            ((v & 0xFF000000) >>> 16);  // 0x0000FF00

        this.value = val ^ 0xA9B8C7D6;
    }

    public float getValue()
    {
        int val = this.value ^ 0xA9B8C7D6;

        int v =
            ((val & 0x00FF0000) >>> 16) +  // 0x000000FF
            ((val & 0x000000FF) << 8)   +  // 0x0000FF00
            ((val & 0xFF000000) >>> 8)  +  // 0x00FF0000
            ((val & 0x0000FF00) << 16);    // 0xFF000000

        return Float.intBitsToFloat(v);
    }

    public void inc()
    {
        setValue(getValue() + 1);
    }

    public void dec()
    {
        setValue(getValue() - 1);
    }
}
