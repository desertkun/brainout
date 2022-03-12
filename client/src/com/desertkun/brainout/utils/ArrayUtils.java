package com.desertkun.brainout.utils;

public class ArrayUtils
{
    public static <T> boolean Contains(final T[] array, final T v) {
        for (final T e : array)
            if (e == v || v != null && v.equals(e))
                return true;

        return false;
    }
}
