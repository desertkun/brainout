package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.active.ActiveData;

public class ActiveUpdateRequestMsg implements UdpMessage
{
    public int activeId;

    public ActiveUpdateRequestMsg() {}

    public ActiveUpdateRequestMsg(ActiveData activeData)
    {
        this.activeId = activeData.getId();
    }

}
