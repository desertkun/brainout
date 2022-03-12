package com.desertkun.brainout.common.msg.server;

public class ClaimOnlineEventResultMsg
{
    public int eventId;
    public int rewardIndex;
    public boolean success;

    public ClaimOnlineEventResultMsg() {}
    public ClaimOnlineEventResultMsg(int eventId, int rewardIndex, boolean success)
    {
        this.eventId = eventId;
        this.rewardIndex = rewardIndex;
        this.success = success;
    }
}
