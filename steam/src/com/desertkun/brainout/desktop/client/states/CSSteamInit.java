package com.desertkun.brainout.desktop.client.states;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamLibraryLoaderLwjgl3;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSError;
import com.desertkun.brainout.client.states.CSOnlineInit;
import com.desertkun.brainout.client.states.ControllerState;

public class CSSteamInit extends ControllerState
{
    private static boolean Inited;

    @Override
    public ID getID()
    {
        return ID.steamInit;
    }

    public static void TryInit()
    {
        if (Inited)
            return;

        try
        {
            if (!SteamAPI.loadLibraries(new SteamLibraryLoaderLwjgl3()))
            {
                return;
            }

            Inited = SteamAPI.init();
        }
        catch (SteamException e)
        {
            e.printStackTrace();
            Inited = false;
        }
    }

    @Override
    public void init()
    {
        try
        {
            if (Inited || SteamAPI.init())
            {
                switchTo(new CSSteamUserInit());
            }
            else
            {
                switchTo(new CSError(L.get("MENU_FAILED_TO_INIT_STEAM")));
            }
        }
        catch (SteamException e)
        {
            switchTo(new CSError(e.getMessage()));
        }
    }

    @Override
    public void release()
    {

    }
}
