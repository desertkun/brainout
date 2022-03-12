package com.desertkun.brainout.utils;

public class DurationUtils
{
    public static String GetDurationString(int seconds)
    {
        if (seconds > 86400)
        {
            int days = seconds / 86400;

            return String.valueOf(days) + "d " + GetDurationString(seconds % 86400);
        }

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(seconds);
    }

    public static String GetShortDurationString(int seconds)
    {
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.valueOf(minutes) + ":" + twoDigitString(seconds);
    }

    private static String twoDigitString(int number)
    {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }
}
