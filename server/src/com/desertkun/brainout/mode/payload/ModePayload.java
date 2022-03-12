package com.desertkun.brainout.mode.payload;

import com.desertkun.brainout.client.Client;
import org.json.JSONObject;

public abstract class ModePayload
{
    private final Client playerClient;

    public ModePayload(Client playerClient)
    {
        this.playerClient = playerClient;
    }

    public Client getPlayerClient()
    {
        return playerClient;
    }

    public abstract void init();
    public abstract void release();
    public void update(float dt) {}

    public void getInfo(JSONObject info) {}
}
