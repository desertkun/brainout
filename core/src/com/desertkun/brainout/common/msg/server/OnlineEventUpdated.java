package com.desertkun.brainout.common.msg.server;

public class OnlineEventUpdated
{
    public int event;
    public float score;

    public OnlineEventUpdated() {}
    public OnlineEventUpdated(int event, float score)
    {
        this.event = event;
        this.score = score;
    }
}
