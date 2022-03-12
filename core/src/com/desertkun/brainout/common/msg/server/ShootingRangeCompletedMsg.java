package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;

public class ShootingRangeCompletedMsg implements ModeMessage
{
    public int hits;

    public ShootingRangeCompletedMsg() {}
    public ShootingRangeCompletedMsg(int hits)
    {
        this.hits = hits;
    }
}
