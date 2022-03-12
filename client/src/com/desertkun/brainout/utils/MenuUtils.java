package com.desertkun.brainout.utils;

import com.desertkun.brainout.Constants;

public class MenuUtils
{
    public static String getStatIcon(String currency)
    {
        switch (currency)
        {
            case Constants.User.SKILLPOINTS:
            {
                return  "skillpoints-small";
            }
            case Constants.User.NUCLEAR_MATERIAL:
            {
                return  "icon-nuclear-material-small";
            }
            case "ru":
            {
                return  "icon-ru-5000";
            }
        }

        return "icon-gears";
    }
}
