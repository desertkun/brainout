package com.desertkun.brainout.online;


import com.esotericsoftware.kryo.Kryo;

import java.io.IOException;

public abstract class NetworkClient
{
    protected final NetworkConnectionListener listener;
    protected final Kryo kryo;

    public interface ConnectListener
    {
        void success();
        void failed();
    }

    public NetworkClient(Kryo kryo, NetworkConnectionListener listener)
    {
        this.listener = listener;
        this.kryo = kryo;
    }

    public NetworkConnectionListener getListener()
    {
        return listener;
    }

    public abstract void connect(int timeout, String host, int tcp, int udp, ConnectListener connectListener);
    public abstract void dispose() throws IOException;
    public abstract void stop();

    public Kryo getKryo()
    {
        return kryo;
    }
}
