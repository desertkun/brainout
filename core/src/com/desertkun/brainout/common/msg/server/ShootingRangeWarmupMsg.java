package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;

public class ShootingRangeWarmupMsg implements ModeMessage
{
    public int time;

    public ShootingRangeWarmupMsg() {}
    public ShootingRangeWarmupMsg(int time)
    {
        this.time = time;
    }
}
