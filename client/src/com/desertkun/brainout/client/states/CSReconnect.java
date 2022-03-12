package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.online.NetworkClient;

import java.util.TimerTask;

public class CSReconnect extends ControllerState
{
    private float timer;

    @Override
    public ID getID()
    {
        return ID.reconnect;
    }

    @Override
    public void init()
    {
        timer = Constants.Connection.RECONNECT_TIME_OUT;

        tryConnect();
    }

    private void tryConnect()
    {
        BrainOutClient.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Gdx.app.postRunnable(() -> doConnect());
            }
        }, 1000);
    }

    private void doConnect()
    {
        ClientController C = BrainOutClient.ClientController;

        BrainOutClient.Network.connect(
            Constants.Connection.TIME_OUT,
            C.getServerLocation(),
            C.getServerTcpPort(),
            C.getServerUdpPort(), new NetworkClient.ConnectListener()
            {
                @Override
                public void success()
                {
                    connected();
                }

                @Override
                public void failed()
                {
                    if (timer > 0)
                    {
                        tryConnect();
                    }
                    else
                    {
                        switchTo(new CSError(L.get("MENU_CONNECTION_ERROR")));
                    }
                }
            }
        );
    }

    private void connected()
    {
        ClientController C = BrainOutClient.ClientController;

        switchTo(new CSConnected(C.getKey(), C.getMyId()));
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;
    }

    @Override
    public void release()
    {

    }
}
