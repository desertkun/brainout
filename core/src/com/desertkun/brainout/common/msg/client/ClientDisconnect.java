package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.enums.DisconnectReason;

public class ClientDisconnect
{
    public DisconnectReason reason;

    public ClientDisconnect() {}
    public ClientDisconnect(DisconnectReason reason)
    {
        this.reason = reason;
    }
}
