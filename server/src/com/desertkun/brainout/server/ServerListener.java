package com.desertkun.brainout.server;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.common.msg.client.HelloMsg;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;
import com.desertkun.brainout.utils.ExceptionHandler;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class ServerListener extends Listener
{
    private final ServerController controller;

    public ServerListener(ServerController controller)
    {
        this.controller = controller;
    }

    @Override
    public void connected(Connection connection)
    {
        //
    }

    @Override
    public void disconnected(Connection connection)
    {
        final Client client = controller.getConnections().get(connection);

        if (client != null)
        {
            if (Log.INFO) Log.info("Client disconnected (" + client.getId() + ")");

            BrainOutServer.PostRunnable(() ->
            {
                if (client.onDisconnect())
                {
                    controller.getClients().releaseClient(client);
                    controller.checkIfIsEmpty();
                }
            });
        }

        controller.getConnections().remove(connection);
    }

    @Override
    public void received(Connection connection, Object object)
    {
        Client client = controller.getConnections().get(connection);

        if (client == null)
        {
            if (object instanceof HelloMsg)
            {
                connected(connection, ((HelloMsg) object));
            }
        }
        else
        {
            if (object instanceof ModeMessage)
            {
                ModeMessage modeMessage = ((ModeMessage) object);

                PlayState playState = BrainOutServer.Controller.getPlayState();

                if (playState.received(client, modeMessage))
                {
                    return;
                }
            }

            try
            {
                client.received(object);
            }
            catch (Exception e)
            {
                BrainOutServer.PostRunnable(() ->
                    ExceptionHandler.reportCrash(e, "crashreport", () -> {}));
            }
        }
    }

    private void connected(Connection connection, HelloMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (msg.reconnect == -1)
            {
                final PlayerClient client = controller.getClients().newPlayerClient();

                if (client != null)
                {
                    controller.getConnections().put(connection, client);

                    client.received(msg);

                    if (Log.INFO) Log.info("New client connected (" + client.getId() + ")");
                }
                else
                {
                    connection.close();

                    if (Log.INFO) Log.info("Failed to add client: no slots left");
                }
            }
            else
            {
                final Client client = controller.getClients().get(msg.reconnect);

                if (client instanceof PlayerClient)
                {
                    PlayerClient playerClient = ((PlayerClient) client);

                    if (BrainOut.OnlineEnabled())
                    {
                        if (!BrainOutServer.getInstance().hasOnlineController() ||
                                (playerClient.getKey() != null && playerClient.getKey().equals(msg.key)))
                        {
                            Connection oldConnection = playerClient.getConnection();
                            if (oldConnection != null)
                            {
                                controller.getConnections().remove(oldConnection);
                            }

                            controller.getConnections().put(connection, playerClient);

                            if (Log.INFO) Log.info("Client reconnected (" + playerClient.getId() + ")");
                            playerClient.reconnected();
                        }
                        else
                        {
                            connection.close();
                        }
                    }
                    else
                    {
                        if (Log.INFO) Log.info("Client reconnected (" + playerClient.getId() + ")");
                        playerClient.setConnection(connection);
                        controller.getConnections().put(connection, playerClient);
                        playerClient.reconnected();
                    }
                }
                else
                {
                    connection.close();
                }
            }
        });
    }

    @Override
    public void idle(Connection connection)
    {
    }
}
