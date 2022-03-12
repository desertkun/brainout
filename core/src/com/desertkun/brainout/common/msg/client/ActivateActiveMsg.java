package com.desertkun.brainout.common.msg.client;

public class ActivateActiveMsg
{
    public int id;
    public String payload;

    public ActivateActiveMsg() {}
    public ActivateActiveMsg(int id, String payload)
    {
        this.id = id;
        this.payload = payload;
    }
}
