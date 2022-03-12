package com.desertkun.brainout.server;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import org.anthillplatform.runtime.server.GameServerController;
import com.desertkun.brainout.BrainOutServer;
import com.esotericsoftware.minlog.Log;

public class BrainOutServerController extends GameServerController
{
    public BrainOutServerController(String socket)
    {
        super(socket);
    }

    @Override
    protected String getStatus()
    {
        boolean haveActiveConnection = false;

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            if (!(entry.value instanceof PlayerClient))
            {
                continue;
            }

            if (entry.value.isConnected())
            {
                haveActiveConnection = true;
                break;
            }
        }

        if (!haveActiveConnection)
        {
            return "empty";
        }

        if (BrainOutServer.Controller.isLobby() || BrainOutServer.Controller.isFreePlay())
        {
            BrainOutServer.Controller.checkDeployment();
        }

        return "ok";
    }

    @Override
    protected void logInfo(String log)
    {
        if (Log.INFO) Log.info(log);
    }

    @Override
    protected void logError(String log)
    {
        if (Log.ERROR) Log.error(log);
    }
}
