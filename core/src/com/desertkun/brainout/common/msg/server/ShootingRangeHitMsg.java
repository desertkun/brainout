package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;

public class ShootingRangeHitMsg implements ModeMessage
{
    public int hits;

    public ShootingRangeHitMsg() {}
    public ShootingRangeHitMsg(int hits)
    {
        this.hits = hits;
    }
}
