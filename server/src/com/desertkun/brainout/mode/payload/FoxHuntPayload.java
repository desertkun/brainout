package com.desertkun.brainout.mode.payload;

import com.desertkun.brainout.client.Client;

public class FoxHuntPayload extends ModePayload
{
    private boolean fox;

    public FoxHuntPayload(Client playerClient)
    {
        super(playerClient);
    }

    @Override
    public void init()
    {

    }

    @Override
    public void release()
    {

    }

    public boolean isFox()
    {
        return fox;
    }

    public void setFox(boolean fox)
    {
        this.fox = fox;
    }
}
