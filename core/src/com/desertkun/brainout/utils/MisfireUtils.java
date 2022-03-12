package com.desertkun.brainout.utils;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

public class MisfireUtils
{

    public static boolean TestMisfire(int quality, int random)
    {
        if (quality == -1)
            return false;

        float prob = 1.0f - Interpolation.sineOut.apply((float)quality / 100.0f);

        int dice = random % 1000;
        return dice < (int)(prob * 50f);
    }
}
