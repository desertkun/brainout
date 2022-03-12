package com.desertkun.brainout.common.msg.server;

public class SpectatorFlagMsg
{
    public boolean s;

    public SpectatorFlagMsg() {}
    public SpectatorFlagMsg(boolean spectator)
    {
        this.s = spectator;
    }
}
