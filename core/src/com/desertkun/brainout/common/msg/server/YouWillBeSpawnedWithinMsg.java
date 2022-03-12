package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;

public class YouWillBeSpawnedWithinMsg implements ModeMessage
{
    public int time;

    public YouWillBeSpawnedWithinMsg() {}
    public YouWillBeSpawnedWithinMsg(int time)
    {
        this.time = time;
    }
}
