package com.desertkun.brainout.common.msg;

public class ReliableReceivedMsg implements UdpMessage
{
    public int messageId;

    public ReliableReceivedMsg() {}
    public ReliableReceivedMsg(int messageId)
    {
        this.messageId = messageId;
    }
}
