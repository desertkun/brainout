package com.desertkun.brainout.utils;

import java.lang.ref.WeakReference;

public class Garbage
{
    public static void gc()
    {
        Object obj = new Object();
        WeakReference ref = new WeakReference<>(obj);
        obj = null;
        while(ref.get() != null) {
            System.gc();
        }
    }
}
