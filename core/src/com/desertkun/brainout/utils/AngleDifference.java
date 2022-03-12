package com.desertkun.brainout.utils;


public class AngleDifference
{
    public static float diff(float a, float b)
    {
        float d = Math.abs(a - b) % 360;
        return d > 180 ? 360 - d : d;
    }
}
