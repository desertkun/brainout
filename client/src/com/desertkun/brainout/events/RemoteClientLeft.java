package com.desertkun.brainout.events;

import com.desertkun.brainout.client.RemoteClient;

public class RemoteClientLeft extends Event
{
    public RemoteClient remoteClient;

    @Override
    public ID getID()
    {
        return ID.remoteClientLeft;
    }

    private Event init(RemoteClient remoteClient)
    {
        this.remoteClient = remoteClient;

        return this;
    }

    public static Event obtain(RemoteClient remoteClient)
    {
        RemoteClientLeft e = obtain(RemoteClientLeft.class);
        if (e == null) return null;
        return e.init(remoteClient);
    }

    @Override
    public void reset()
    {
        remoteClient = null;
    }
}
