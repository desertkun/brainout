package com.desertkun.brainout.common.msg.server;

public class AnalyticsResourceEventMsg
{
    public int amount;
    public String currency;
    public String itemType;
    public String itemId;

    public AnalyticsResourceEventMsg() {}
    public AnalyticsResourceEventMsg(String currency, String itemType, String itemId, int amount)
    {
        this.amount = amount;
        this.currency = currency;
        this.itemType = itemType;
        this.itemId = itemId;
    }
}
