package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;

public class StartPartyResultMsg implements ModeMessage
{
    public boolean success;

    public StartPartyResultMsg() {}
    public StartPartyResultMsg(boolean success)
    {
        this.success = success;
    }
}
