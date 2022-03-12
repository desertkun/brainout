package com.desertkun.brainout.mode.payload;

import com.desertkun.brainout.client.Client;

public class DuelPayload extends ModePayload
{
    private String room;

    public DuelPayload(Client playerClient)
    {
        super(playerClient);

        room = "";
    }

    @Override
    public void init()
    {

    }

    @Override
    public void release()
    {

    }

    public String getRoom()
    {
        return room;
    }

    public void setRoom(String room)
    {
        this.room = room;
    }
}
