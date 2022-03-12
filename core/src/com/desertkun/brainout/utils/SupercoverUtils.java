package com.desertkun.brainout.utils;

public class SupercoverUtils
{
    public interface PointCallback
    {
        void found(float x, float y);
    }

    public static void SupercoverLine(
            float from_x,
            float from_y,
            float to_x,
            float to_y,
            float cell_width,
            float cell_height,
            PointCallback callback)
    {
        float x0  = from_x/cell_width;
        float y0  = from_y/cell_height;
        float x1  = to_x/cell_width;
        float y1  = to_y/cell_height;
        float dx  = Math.abs(x1 - x0);
        float dy  = Math.abs(y1 - y0);
        int   x   = IPart(x0);
        int   y   = IPart(y0);
        int   num = 1;
        int   sx,sy;
        float err;

        if(dx == 0)
        {
            sx  = 0;
            err = Float.POSITIVE_INFINITY;
        }
        else if(x1 > x0)
        {
            sx  = 1;
            num += IPart(x1) - x;
            err = RFpart(x0)*dy;
        }
        else
        {
            sx  = -1;
            num += x - IPart(x1);
            err = FPart(x0)*dy;
        }

        if(dy == 0)
        {
            sy  = 0;
            err = Float.NEGATIVE_INFINITY;
        }
        else if(y1 > y0)
        {
            sy  = 1;
            num += IPart(y1) - y;
            err -= RFpart(y0)*dx;
        }
        else
        {
            sy  = -1;
            num += y - IPart(y1);
            err -= FPart(y0)*dx;
        }

        // number of square to be plotted : num

        while(true)
        {
            callback.found(x,y);

            if(--num == 0) break;

            if(err > 0)
            {
                err -= dx;
                y   += sy;
            }
            else
            {
                err += dy;
                x   += sx;
            }
        }
    }

    private static int IPart(float value)
    {
        return (int)Math.floor(value);
    }

    private static float FPart(float value)
    {
        return value - IPart(value);
    }

    private static float RFpart(float value)
    {
        return 1.0f - FPart(value);
    }
}
