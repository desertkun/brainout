package com.desertkun.brainout.client;

import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryonet.Connection;

public class ConnectionList extends ObjectMap<Connection, PlayerClient>
{
    public ConnectionList()
    {

    }

    @Override
    public PlayerClient put(Connection key, PlayerClient value)
    {
        value.setConnection(key);

        return super.put(key, value);
    }

    @Override
    public PlayerClient remove(Connection key)
    {
        PlayerClient removed = super.remove(key);

        if (removed != null)
        {
            removed.setConnection(null);
        }

        return removed;
    }
}
