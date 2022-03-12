package com.desertkun.brainout.online;

public abstract class NetworkConnection
{
    public abstract void close();
    public abstract String getHost();

    public abstract int sendTCP(Object object);
    public abstract int sendUDP(Object object);
}
