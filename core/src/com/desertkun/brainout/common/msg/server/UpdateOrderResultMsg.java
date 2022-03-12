package com.desertkun.brainout.common.msg.server;

public class UpdateOrderResultMsg
{
    public boolean success;
    public String store;
    public String currency;
    public long orderId;
    public int total;
    public String item;

    public UpdateOrderResultMsg() {}

    public UpdateOrderResultMsg(boolean success, String store, long orderId,
                                String currency, int total, String item)
    {
        this.success = success;
        this.store = store;
        this.orderId = orderId;
        this.currency = currency;
        this.total = total;
        this.item = item;
    }
}
