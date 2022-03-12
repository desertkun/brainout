package com.desertkun.brainout.online;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

public class KryoNetworkClient extends NetworkClient
{
    private Client client;
    private ObjectMap<Connection, KryoNetworkConnection> connections;
    protected ConnectListener connectListener;

    public KryoNetworkClient(Kryo kryo, final NetworkConnectionListener listener)
    {
        super(kryo, listener);

        this.client = new Client(323840, 80480, new KryoSerialization(kryo));
        this.connections = new ObjectMap<>();
        this.connectListener = null;

        client.addListener(new Listener.ThreadedListener(new Listener()
        {
            @Override
            public void connected(Connection connection)
            {
                KryoNetworkConnection newConnection = new KryoNetworkConnection(connection);
                connections.put(connection, newConnection);

                listener.connected(newConnection);

                if (connectListener != null)
                {
                    Gdx.app.postRunnable(() ->
                    {
                        if (connectListener == null)
                            return;

                        connectListener.success();
                        connectListener = null;
                    });
                }

            }

            @Override
            public void disconnected(Connection connection)
            {
                KryoNetworkConnection conn = connections.get(connection);

                if (conn != null)
                {
                    connections.remove(connection);
                    listener.disconnected(conn);
                }
            }

            @Override
            public void received(Connection connection, Object object)
            {
                KryoNetworkConnection conn = connections.get(connection);

                if (conn != null)
                {
                    listener.received(conn, object);
                }
            }
        }));

        client.start();
    }

    @Override
    public void connect(final int timeout, final String host, final int tcp, final int udp,
                        final ConnectListener connectListener)
    {
        new Thread(() ->
        {
            this.connectListener = connectListener;

            try
            {
                client.connect(timeout, host, tcp, udp);
            }
            catch (IOException e)
            {
                System.err.println("Failed to connect to the host: " + host);
                e.printStackTrace();

                Gdx.app.postRunnable(connectListener::failed);
            }
        }).start();
    }

    @Override
    public void stop()
    {
        client.stop();
    }

    @Override
    public void dispose() throws IOException
    {
        client.dispose();
    }

    @Override
    public Kryo getKryo()
    {
        return client.getKryo();
    }
}
