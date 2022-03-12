package com.desertkun.brainout.common.msg.server;

public class NewOrderResultMsg
{
    public boolean success;
    public long orderId;
    public String reason;

    public NewOrderResultMsg() {}

    public NewOrderResultMsg(boolean success, long orderId, String reason)
    {
        this.success = success;
        this.orderId = orderId;
        this.reason = reason;
    }
}
