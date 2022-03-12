package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;

public class DuelCompletedMsg implements ModeMessage
{
    public int loser;
    public int reward;
    public boolean tryAgain;

    public DuelCompletedMsg() {}
    public DuelCompletedMsg(int loser, int reward, boolean tryAgain)
    {
        this.loser = loser;
        this.reward = reward;
        this.tryAgain = tryAgain;
    }
}
