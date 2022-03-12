package com.desertkun.brainout;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.ReliableManager;
import com.desertkun.brainout.common.msg.ReliableUdpMessage;
import com.esotericsoftware.kryonet.Connection;

public class ServerReliableManager extends ReliableManager
{
    @Override
    protected void doDeliver(ReliableUdpMessage message, MessageRecipient messageRecipient)
    {
        ((PlayerClient) messageRecipient).sendUDP(message);
    }

    @Override
    protected void onDisconnect(MessageRecipient messageRecipient)
    {
        Connection connection = ((PlayerClient) messageRecipient).getConnection();

        if (connection != null)
        {
            connection.close();
        }
    }
}
