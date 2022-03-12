package com.desertkun.brainout.utils;

import com.desertkun.brainout.BrainOutClient;
import org.anthillplatform.runtime.services.LoginService;

public class EarlyAccess
{
    public static boolean Have()
    {
        if (!BrainOutClient.LocalizationMgr.getCurrentLanguage().equals("RU"))
        {
            return false;
        }

        String account = BrainOutClient.ClientController.getMyAccount();

        int asInt;

        try
        {
            asInt = Integer.valueOf(account);
        } catch (NumberFormatException e)
        {
            return false;
        }

        return asInt < 1000 || asInt % 10 == 0;
    }
}
