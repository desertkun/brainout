package com.desertkun.brainout.online;

import com.esotericsoftware.kryonet.Connection;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class KryoNetworkConnection extends NetworkConnection
{
    private final Connection connection;

    public KryoNetworkConnection(Connection connection)
    {
        this.connection = connection;
    }

    public Connection getConnection()
    {
        return connection;
    }

    @Override
    public void close()
    {
        connection.close();
    }

    @Override
    public String getHost()
    {
        if (connection == null)
            return null;

        InetSocketAddress tcp = connection.getRemoteAddressTCP();

        if (tcp == null)
            return null;

        InetAddress address = tcp.getAddress();

        if (address == null)
            return null;

        return address.getHostAddress();
    }

    @Override
    public int sendTCP(Object object)
    {
        return connection.sendTCP(object);
    }

    @Override
    public int sendUDP(Object object)
    {
        return connection.sendUDP(object);
    }
}
