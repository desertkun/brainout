package com.desertkun.brainout.data.interfaces;

public interface WithTag
{
    public static final int MAX_TAGS = 32;
    static int TAG(int tag)
    {
        return 1 << tag;
    }

    int getTags();
}
