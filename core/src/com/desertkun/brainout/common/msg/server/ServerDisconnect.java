package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.enums.DisconnectReason;

public class ServerDisconnect
{
    public DisconnectReason reason;

    public ServerDisconnect() {}
    public ServerDisconnect(DisconnectReason reason)
    {
        this.reason = reason;
    }
}
