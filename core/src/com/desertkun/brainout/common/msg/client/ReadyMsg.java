package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.ModeMessage;

public class ReadyMsg implements ModeMessage
{
    public boolean ready;

    public ReadyMsg() {}
    public ReadyMsg(boolean ready)
    {
        this.ready = ready;
    }
}
