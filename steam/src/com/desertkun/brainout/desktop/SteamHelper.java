package com.desertkun.brainout.desktop;

import com.codedisaster.steamworks.SteamID;

public class SteamHelper
{
    public static String getSteamIdCredential(SteamID id)
    {
        return String.valueOf(SteamID.getNativeHandle(id));
    }
}
