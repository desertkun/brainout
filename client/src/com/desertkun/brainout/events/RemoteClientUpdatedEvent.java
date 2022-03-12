package com.desertkun.brainout.events;

import com.desertkun.brainout.client.RemoteClient;

public class RemoteClientUpdatedEvent extends Event
{
    public RemoteClient remoteClient;

    @Override
    public ID getID()
    {
        return ID.remoteClientUpdated;
    }

    private Event init(RemoteClient remoteClient)
    {
        this.remoteClient = remoteClient;

        return this;
    }

    public static Event obtain(RemoteClient remoteClient)
    {
        RemoteClientUpdatedEvent e = obtain(RemoteClientUpdatedEvent.class);
        if (e == null) return null;
        return e.init(remoteClient);
    }

    @Override
    public void reset()
    {
        remoteClient = null;
    }
}
