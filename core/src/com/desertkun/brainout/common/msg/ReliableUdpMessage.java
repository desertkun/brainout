package com.desertkun.brainout.common.msg;

public class ReliableUdpMessage implements UdpMessage
{
    public int id;
    public ReliableBody body;

    public ReliableUdpMessage() {}
    public ReliableUdpMessage(int id, ReliableBody body)
    {
        this.id = id;
        this.body = body;
    }
}
