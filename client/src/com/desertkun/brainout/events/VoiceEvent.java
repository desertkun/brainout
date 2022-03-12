package com.desertkun.brainout.events;

import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;

public class VoiceEvent extends Event
{
    public RemoteClient remoteClient;
    public ActiveData playerData;

    @Override
    public Event.ID getID()
    {
        return Event.ID.voice;
    }

    private Event init(RemoteClient remoteClient, ActiveData playerData)
    {
        this.remoteClient = remoteClient;
        this.playerData = playerData;

        return this;
    }

    public static Event obtain(RemoteClient remoteClient, ActiveData playerData)
    {
        VoiceEvent e = obtain(VoiceEvent.class);
        if (e == null) return null;
        return e.init(remoteClient, playerData);
    }

    @Override
    public void reset()
    {
        this.remoteClient = null;
        this.playerData = null;
    }
}
