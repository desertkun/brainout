package com.desertkun.brainout.playstate;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSEndGame;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.menu.ui.Avatars;

public class ClientPSEndGame extends PlayStateEndGame
{
    @Override
    public void init(InitCallback done)
    {
        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
        BrainOutClient.ClientController.setState(new CSEndGame(this, csGame));
    }

    @Override
    public void release()
    {
        BrainOutClient.ClientController.clear();

        Avatars.Reset();
    }
}
