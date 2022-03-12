package com.desertkun.brainout.data.interfaces;

public class FlippedAngle
{
    public static float getAngle(float from, boolean flipX)
    {
        if (flipX)
        {
            return 180 - from;
        }

        return from;
    }
}
