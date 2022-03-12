package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;

public class ShootingRangeStartedMsg implements ModeMessage
{
    public int time;

    public ShootingRangeStartedMsg() {}
    public ShootingRangeStartedMsg(int time)
    {
        this.time = time;
    }
}
