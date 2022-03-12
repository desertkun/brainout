package com.desertkun.brainout.desktop.client.states;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSOnlineInit;
import com.desertkun.brainout.client.states.CSPrivacyPolicy;
import com.desertkun.brainout.client.states.ControllerState;
import com.desertkun.brainout.desktop.SteamEnvironment;

public class CSSteamStats extends ControllerState
{
    @Override
    public ID getID()
    {
        return ID.steamStats;
    }

    @Override
    public void init()
    {
        SteamEnvironment env = ((SteamEnvironment) BrainOutClient.Env);

        env.getGameUser().initSocial();

        switchTo(new CSPrivacyPolicy());
    }

    @Override
    public void release()
    {

    }
}
