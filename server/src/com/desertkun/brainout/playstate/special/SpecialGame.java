package com.desertkun.brainout.playstate.special;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.data.active.PlayerData;

public abstract class SpecialGame
{
    public abstract void init();
    public abstract void release();

    public void onClientDeath(Client client, PlayerData playerData) {}
}
