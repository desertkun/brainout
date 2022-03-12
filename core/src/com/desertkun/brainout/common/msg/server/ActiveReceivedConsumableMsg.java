package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.enums.EntityReceived;
import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.active.ActiveData;

public class ActiveReceivedConsumableMsg implements UdpMessage
{
    public int activeId;
    public int d;
    public EntityReceived entityReceived;
    public int amount;

    public ActiveReceivedConsumableMsg() {}
    public ActiveReceivedConsumableMsg(ActiveData activeData, EntityReceived entityReceived, int amount)
    {
        this.activeId = activeData.getId();
        this.d = activeData.getDimensionId();
        this.entityReceived = entityReceived;
        this.amount = amount;
    }

}
