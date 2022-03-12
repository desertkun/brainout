package com.desertkun.brainout.common.msg.client;

public class UpdateOrderMsg
{
    public long orderId;

    public UpdateOrderMsg() {}

    public UpdateOrderMsg(long orderId)
    {
        this.orderId = orderId;
    }
}
