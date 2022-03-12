package com.desertkun.brainout.utils;

public class ProtectedInt
{
    private int value;

    public ProtectedInt(int value)
    {
        setValue(value);
    }

    public void setValue(int value)
    {
        int val =
            ((value & 0x000000FF) << 16) +  // 0x00FF0000
            ((value & 0x0000FF00) >>> 8) +  // 0x000000FF
            ((value & 0x00FF0000) << 8)  +  // 0xFF000000
            ((value & 0xFF000000) >>> 16);  // 0x0000FF00

        this.value = val ^ 0xA9B8C7D6;
    }

    public int getValue()
    {
        int val = this.value ^ 0xA9B8C7D6;

        return
            ((val & 0x00FF0000) >>> 16) +  // 0x000000FF
            ((val & 0x000000FF) << 8)   +  // 0x0000FF00
            ((val & 0xFF000000) >>> 8)  +  // 0x00FF0000
            ((val & 0x0000FF00) << 16);    // 0xFF000000
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
