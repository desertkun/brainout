package com.desertkun.brainout.client.states;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.online.NetworkClient;

public class CSConnecting extends ControllerState
{
    private final String serverLocation;
    private final int tcp;
    private final int udp;
    private final String key;
    private final int reconnect;
    private final Runnable onConnectionFailed;

    public CSConnecting(String serverLocation, int tcp, int udp, String key, int reconnect,
                        Runnable onConnectionFailed)
    {
        this.serverLocation = serverLocation;
        this.tcp = tcp;
        this.udp = udp;
        this.key = key;
        this.reconnect = reconnect;
        this.onConnectionFailed = onConnectionFailed;
    }

    @Override
    public void init()
    {
        BrainOut.Network.connect(Constants.Connection.TIME_OUT, serverLocation, tcp, udp,
            new NetworkClient.ConnectListener()
        {
            @Override
            public void success()
            {
                connected();
            }

            @Override
            public void failed()
            {
                if (onConnectionFailed != null)
                {
                    onConnectionFailed.run();
                }
                else
                {
                    switchTo(new CSError(L.get("MENU_CONNECTION_ERROR")));
                }
            }
        });
    }

    @Override
    public void release()
    {
        //
    }

    public void connected()
    {
        switchTo(new CSConnected(key, reconnect));
    }

    @Override
    public ID getID()
    {
        return ID.connecting;
    }
}
