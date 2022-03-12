package com.desertkun.brainout.common.msg.server;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.consumable.ConsumableContainer;

public class ConsumablesUpdateMsg implements UdpMessage
{
    public String data;

    public ConsumablesUpdateMsg() {}
    public ConsumablesUpdateMsg(ConsumableContainer container)
    {

        this.data = BrainOut.R.JSON.toJson(container);
    }
}
