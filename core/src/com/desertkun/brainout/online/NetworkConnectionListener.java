package com.desertkun.brainout.online;

public interface NetworkConnectionListener
{
    void connected(NetworkConnection connection);
    void disconnected(NetworkConnection connection);
    void received(NetworkConnection connection, Object object);
}
