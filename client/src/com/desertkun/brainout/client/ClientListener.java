package com.desertkun.brainout.client;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.online.NetworkConnection;
import com.desertkun.brainout.online.NetworkConnectionListener;
import com.desertkun.brainout.utils.ExceptionHandler;

public class ClientListener implements NetworkConnectionListener
{
    private final ClientController clientController;

    public ClientListener(ClientController clientController)
    {
        this.clientController = clientController;
    }

    @Override
    public void connected(final NetworkConnection connection)
    {
        Gdx.app.log("connection", "Connected to server " + (connection != null ? connection.getHost() : "[unknown]"));

        Gdx.app.postRunnable(() -> clientController.onConnect(connection));
    }

    @Override
    public void disconnected(NetworkConnection connection)
    {
        Gdx.app.log("connection", "Disconnected from server");

        Gdx.app.postRunnable(clientController::onDisconnect);
    }

    @Override
    public void received(NetworkConnection connection, Object object)
    {
        try
        {
            clientController.received(object);
        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e);
        }
    }
}
