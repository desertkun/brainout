package com.desertkun.brainout.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils
{
    public static String formatInterval(final long l)
    {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        return String.format("%02d:%02d:%02d", hr, min, sec);
    }

    public static String formatMinutesInterval(final long l)
    {
        final long min = TimeUnit.MILLISECONDS.toMinutes(l);
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.MINUTES.toMillis(min));
        return String.format("%02d:%02d", min, sec);
    }
}
