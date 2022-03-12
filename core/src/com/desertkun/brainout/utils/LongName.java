package com.desertkun.brainout.utils;

public class LongName
{
    public static String limit(String name, int length)
    {
        if (name.length() > length)
        {
            return name.substring(0, Math.max(0, length - 3)) + "...";
        }

        return name;
    }
}
